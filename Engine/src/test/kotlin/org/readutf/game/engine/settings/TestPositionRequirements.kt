package org.readutf.game.engine.settings

import net.minestom.server.coordinate.Vec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.readutf.game.engine.settings.location.PositionSettings
import org.readutf.game.engine.settings.location.PositionType
import java.nio.file.Files

class TestPositionRequirements {
    @Test
    fun generateValidPositionRequirements() {
        val result = gameSettingsManager.generatePositionRequirements(PositionRequirements::class)
        Assertions.assertEquals(true, result.isSuccess)
    }

    @Test
    fun generateInvalidPositionRequirements() {
        val result = gameSettingsManager.generatePositionRequirements(MissingPositionRequirements::class)
        Assertions.assertEquals(true, result.isFailure)
    }

    @Test
    fun generateInvalidTypePositionRequirements() {
        val result = gameSettingsManager.generatePositionRequirements(InvalidTypePositionRequirements::class)
        Assertions.assertEquals(true, result.isFailure)
    }

    data class PositionRequirements(
        @PositionType("testPosition") val testPosition: Vec,
        @PositionType("testListPositions") val listPositions: List<Vec>,
    ) : PositionSettings

    data class MissingPositionRequirements(
        val testPosition: Vec,
        val listPositions: List<Vec>,
    ) : PositionSettings

    data class InvalidTypePositionRequirements(
        val testPosition: Vec,
        val listPositions: String,
    ) : PositionSettings

    companion object {
        @JvmStatic val gameSettingsManager = GameSettingsManager(Files.createTempDirectory("game-settings").toFile())
    }
}
