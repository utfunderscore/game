package org.readutf.game.engine.game.settings

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.readutf.game.engine.game.settings.location.PositionSettings
import org.readutf.game.engine.game.settings.location.PositionType
import org.readutf.game.engine.game.settings.location.PositionTypeData
import org.readutf.game.engine.game.settings.test.DualGamePositions
import org.readutf.game.engine.game.settings.test.DualGameSettings
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
