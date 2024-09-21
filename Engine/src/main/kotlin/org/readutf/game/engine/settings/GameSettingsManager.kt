package org.readutf.game.engine.settings

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.readutf.game.engine.settings.location.PositionSettings
import org.readutf.game.engine.settings.location.PositionType
import org.readutf.game.engine.settings.location.PositionTypeData
import org.readutf.game.engine.settings.test.DualGamePositions
import org.readutf.game.engine.settings.test.DualGameSettings
import org.readutf.game.engine.types.Position
import org.readutf.game.engine.types.Result
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

class GameSettingsManager(
    baseFile: File,
) {
    private val settingsFolder = File(baseFile, "game-settings")

    private val defaultGameData = mutableMapOf<String, Any>()
    private val positionRequirements = mutableMapOf<String, List<PositionTypeData>>()

    private val objectMapper =
        jsonMapper {
            addModule(kotlinModule())
        }

    init {
        if (!settingsFolder.exists()) {
            settingsFolder.mkdirs()
        }
        if (!settingsFolder.isDirectory) {
            throw IllegalArgumentException("Game settings folder is not a directory")
        }

        registerGameDefaults("dual", DualGameSettings(), DualGamePositions::class)
    }

    fun registerGameDefaults(
        gameName: String,
        gameDataDefaults: Any,
        positionSettings: KClass<out PositionSettings>,
    ): Result<Unit> {
        defaultGameData[gameName] = gameDataDefaults
        val generatePositionRequirements = generatePositionRequirements(positionSettings)
        if (generatePositionRequirements.isFailure) return Result.failure(generatePositionRequirements.getError())
        positionRequirements[gameName] = generatePositionRequirements.getValue()

        val gameSettingsFolder =
            File(settingsFolder, gameName).apply {
                if (!exists()) mkdirs()
            }

        // Save custom game data defaults to game-data.json

        File(gameSettingsFolder, "game-data.json").apply {
            if (!exists()) objectMapper.writeValue(this, gameDataDefaults)
        }

        // Save position requirements to position-requirements.json

        File(gameSettingsFolder, "position-requirements.json").apply {
            if (!exists()) objectMapper.writeValue(this, generatePositionRequirements.getValue())
        }

        return Result.success(Unit)
    }

    fun <T : PositionSettings> loadPositionSettings(
        positions: Map<String, Position>,
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

        val parameters = mutableListOf<Position>()

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
            parameters.add(position)
        }

        return Result.success(constructor.call(*parameters.toTypedArray()))
    }

    fun validatePositionRequirements(
        gameName: String,
        positions: Map<String, Position>,
    ): Result<Unit> {
        val requirements = getPositionRequirements(gameName).onFailure { return Result.failure(it) }

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

    private fun getPositionRequirements(gameName: String): Result<List<PositionTypeData>> {
        val gameSettingsFolder = File(settingsFolder, gameName)
        val positionRequirementsFile = File(gameSettingsFolder, "position-requirements.json")

        return if (positionRequirementsFile.exists()) {
            val positionRequirements =
                objectMapper.readValue(positionRequirementsFile, object : TypeReference<List<PositionTypeData>>() {})
            Result.success(positionRequirements)
        } else {
            positionRequirements[gameName]?.let {
                Result.success(it)
            } ?: Result.failure("Position requirements not found for game: $gameName")
        }
    }

    fun generatePositionRequirements(settingsClass: KClass<out PositionSettings>): Result<List<PositionTypeData>> {
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
