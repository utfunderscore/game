package org.readutf.game.engine.settings

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.Position
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.types.Result
import kotlin.jvm.Throws
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*

class PositionSettingsManager {
    private val logger = KotlinLogging.logger {}
    private val positionTypes = mutableMapOf<String, List<Regex>>()

    /**
     * Registering the requirements of a position data class
     * Doing so allows builders to check that the build they have created
     * has the correct markers placed.
     */
    @Throws(Exception::class)
    fun <T : PositionData> registerRequirements(
        gameType: String,
        positionRequirements: KClass<T>,
    ): Result<List<Regex>> {
        logger.info { "Registering requirements for ${positionRequirements.simpleName}" }

        val primary = positionRequirements.primaryConstructor ?: return Result.failure("Primary constructor not found")

        val requirements = mutableListOf<Regex>()

        for (parameter in primary.parameters) {
            val classifier = parameter.type.classifier
            if (classifier !is KClass<*>) {
                return Result.failure("${parameter.name} is not a valid type")
            }

            if (classifier.isSubclassOf(PositionData::class)) {
                requirements.addAll(registerRequirements("reserved", classifier as KClass<out PositionData>).mapError { return it })
                continue
            }

            val annotation =
                parameter.findAnnotation<Position>()
                    ?: return Result.failure(
                        "${parameter.name} in ${positionRequirements::class.simpleName} is missing the @Position annotation",
                    )

            when {
                annotation.name != "" -> requirements.add(Regex("^${annotation.name}$"))
                annotation.startsWith != "" -> requirements.add(Regex("${annotation.startsWith}.*"))
                annotation.endsWith != "" -> requirements.add(Regex(".*${annotation.endsWith}"))
                else -> return Result.failure("Invalid position annotation, a filter must be set")
            }
        }

        positionTypes[gameType] = requirements
        positionTypes[positionRequirements.qualifiedName!!] = requirements

        return Result.success(requirements)
    }

    fun <T : PositionData> loadPositionData(
        positions: Map<String, Marker>,
        positionSettingsType: KClass<out T>,
    ): Result<T> {
        if (positionSettingsType.qualifiedName == null) {
            return Result.failure("Invalid position settings type")
        }

        val primaryConstructor = positionSettingsType.primaryConstructor ?: return Result.failure("Primary constructor not found")

        val parameters = mutableListOf<Any>()

        for (parameter in primaryConstructor.parameters) {
            val classifier = parameter.type.classifier
            if (classifier !is KClass<*> ||
                (
                    classifier != List::class &&
                        classifier != Marker::class &&
                        !classifier.isSubclassOf(PositionData::class)
                )
            ) {
                return Result.failure("Invalid type for ${parameter.name}")
            }

            if (classifier.isSubclassOf(PositionData::class)) {
                val subClass = classifier as KClass<out PositionData>
                parameters.add(loadPositionData(positions, subClass).mapError { return it })
            } else {
                val annotation = parameter.findAnnotation<Position>() ?: return Result.failure("Missing @Position annotation")
                val regex =
                    when {
                        annotation.name != "" -> Regex("^${annotation.name}$")
                        annotation.startsWith != "" -> Regex("${annotation.startsWith}.*")
                        annotation.endsWith != "" -> Regex(".*${annotation.endsWith}")
                        else -> return Result.failure("Invalid position annotation, a filter must be set")
                    }
                parameters.add(getParameterForType(regex, parameter, positions).mapError { return it })
            }
        }

        println(parameters)

        try {
            val positionData = primaryConstructor.call(*parameters.toTypedArray())
            return Result.success(positionData)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create position data" }
            return Result.failure("Failed to create position data: ${e.message}")
        }
    }

    fun validatePositionRequirements(
        gameType: String,
        positions: Map<String, Marker>,
    ): Result<Unit> {
        val types = positionTypes[gameType] ?: return Result.failure("No position requirements found for $gameType")

        types.forEach { regex ->
            val matchingPositions = positions.filter { it.key.matches(regex) }

            if (matchingPositions.isEmpty()) {
                return Result.failure("No positions found matching $regex")
            }
            println("matching $regex + $matchingPositions")
        }
        return Result.success(Unit)
    }

    private fun getParameterForType(
        regex: Regex,
        parameter: KParameter,
        positions: Map<String, Marker>,
    ): Result<Any> {
        val isList = parameter.type.classifier == List::class

        if (isList) {
            val values = positions.filter { it.key.matches(regex) }.values

            if (values.isEmpty()) {
                return Result.failure("No positions found for ${parameter.name} matching $regex")
            }
            return Result.success(values.toList())
        } else {
            val multipleOptions = positions.filter { it.key.matches(regex) }.values

            if (multipleOptions.isEmpty()) {
                return Result.failure("No positions found for ${parameter.name} matching $regex")
            }
            if (multipleOptions.size > 1) {
                logger.warn { "Multiple positions found for ${parameter.name} matching $regex" }
            }

            val value = multipleOptions.first()

            return Result.success(value)
        }
    }
}
