package org.readutf.game.engine.settings

import net.minestom.server.coordinate.Vec
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.settings.location.PositionType
import org.readutf.game.engine.settings.location.PositionTypeData
import org.readutf.game.engine.types.Result
import kotlin.jvm.Throws
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

class PositionSettingsManager {
    // Maps a specific game to its positionRequirements
    val gameRequirements = mutableMapOf<String, List<PositionTypeData>>()

    @Throws(Exception::class)
    fun <T : PositionData> registerRequirements(
        gameType: String,
        positionRequirements: KClass<T>,
    ): Result<List<PositionTypeData>> {
        val requirements = generatePositionRequirements(positionRequirements).mapError { return it }
        gameRequirements[gameType] = requirements
        return Result.success(requirements)
    }

    fun <T : PositionData> loadPositionData(
        positions: Map<String, Marker>,
        positionSettingsType: KClass<out T>,
    ): Result<T> {
        if (!positionSettingsType.isData) return Result.failure("Position Settings class is not a data class")
        val constructor = positionSettingsType.primaryConstructor ?: return Result.failure("Could not find a constructor")

        if (constructor.parameters.any {
                it.findAnnotation<PositionType>() == null
            }
        ) {
            return Result.failure("All parameters must be annotated with @PositionType")
        }

        val parameters = mutableListOf<Vec>()

        for (parameter in constructor.parameters) {
            val positionType =
                parameter.findAnnotation<PositionType>()
                    ?: return Result.failure("Parameter ${parameter.name} does not have a PositionType annotation")
            val position =
                when {
                    positionType.name.isNotEmpty() -> positions[positionType.name]
                    positionType.startsWith.isNotEmpty() ->
                        positions.entries
                            .firstOrNull {
                                it.key.startsWith(
                                    positionType.startsWith,
                                )
                            }?.value
                    positionType.endsWith.isNotEmpty() -> positions.entries.firstOrNull { it.key.endsWith(positionType.endsWith) }?.value
                    else -> return Result.failure("PositionType annotation must have a name, startsWith, or endsWith value")
                } ?: return Result.failure("Could not find a position for parameter ${parameter.name}")
            parameters.add(position.position)
        }

        return Result.success(constructor.call(*parameters.toTypedArray()))
    }

    fun validatePositionRequirements(
        gameType: String,
        positions: Map<String, Marker>,
    ): Result<Unit> {
        val requirements = gameRequirements[gameType] ?: return Result.failure("Could not find any positions for $gameType")
        for (requirement in requirements) {
            when {
                requirement.name.isNotEmpty() && !positions.containsKey(requirement.name) ->
                    return Result.failure("Position ${requirement.name} is required")
                requirement.startsWith.isNotEmpty() && positions.none { it.key.startsWith(requirement.startsWith) } ->
                    return Result.failure("At least 1 position starting with ${requirement.startsWith} is required")
                requirement.endsWith.isNotEmpty() && positions.none { it.key.endsWith(requirement.endsWith) } ->
                    return Result.failure("At least 1 position ending with ${requirement.endsWith} is required")
            }
        }

        return Result.success(Unit)
    }

    private fun generatePositionRequirements(settingsClass: KClass<out PositionData>): Result<List<PositionTypeData>> {
        if (!settingsClass.isData) return Result.failure("Settings class is not a data class")

        val positionRequirements = mutableListOf<PositionTypeData>()

        val constructor = settingsClass.primaryConstructor ?: return Result.failure("Settings class does not have a primary constructor")

        for (parameter in constructor.parameters) {
            val positionType =
                parameter.findAnnotation<PositionType>()
                    ?: return Result.failure("Parameter ${parameter.name} does not have a PositionType annotation")

            positionRequirements.add(PositionTypeData.convert(positionType))
        }

        return positionRequirements.let { Result.success(it) }
    }
}
