package org.readutf.game.engine.settings

import net.minestom.server.coordinate.Vec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.readutf.game.engine.settings.location.PositionType

class TestValidPositionData {
    @Test
    fun generateValidPositionRequirements() {
        val result = gameSettingsManager.registerRequirements("valid", ValidPositionData::class)
        Assertions.assertEquals(true, result.isSuccess)
    }

    @Test
    fun generateInvalidPositionRequirements() {
        val result = gameSettingsManager.registerRequirements("missingPosition", MissingPositionData::class)
        Assertions.assertEquals(true, result.isFailure)
    }

    @Test
    fun generateInvalidTypePositionRequirements() {
        val result = gameSettingsManager.registerRequirements("invalidPositions", InvalidTypePositionData::class)
        Assertions.assertEquals(true, result.isFailure)
    }

    data class ValidPositionData(
        @PositionType("testPosition") val testPosition: Vec,
        @PositionType("testListPositions") val listPositions: List<Vec>,
    ) : org.readutf.game.engine.settings.location.PositionData

    data class MissingPositionData(
        val testPosition: Vec,
        val listPositions: List<Vec>,
    ) : org.readutf.game.engine.settings.location.PositionData

    data class InvalidTypePositionData(
        val testPosition: Vec,
        val listPositions: String,
    ) : org.readutf.game.engine.settings.location.PositionData

    companion object {
        @JvmStatic val gameSettingsManager = PositionSettingsManager()
    }
}
