package org.readutf.game.engine.settings

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.PositionData
import org.readutf.game.engine.settings.location.PositionMarker
import org.readutf.game.engine.utils.Position

class TestValidPositionMarkerData {
    @Test
    fun registerValidPositionData() {
        val result = gameSettingsManager.registerRequirements("valid", ValidPositionData::class)
        println(result.getOrNull())
        Assertions.assertEquals(true, result.isSuccess)
    }

    @Test
    fun registerMissingPointData() {
        val result = gameSettingsManager.registerRequirements("missingPosition", MissingPositionData::class)
        Assertions.assertEquals(true, result.isFailure)
    }

    @Test
    fun registerInvalidPositionsData() {
        val result = gameSettingsManager.registerRequirements("invalidPositions", InvalidTypePositionData::class)
        Assertions.assertEquals(true, result.isFailure)
    }

    @Test
    fun loadValidPositionData() {
        val result = gameSettingsManager.registerRequirements("valid", ValidPositionData::class)
        val requirements = result.getOrNull() ?: return
        val positions =
            mapOf(
                "testPosition" to Marker(Position(0.0, 0.0, 0.0), Position(0.0, 0.0, 0.0)),
                "testListPositions" to Marker(Position(0.0, 0.0, 0.0), Position(0.0, 0.0, 0.0)),
                "endsWithPosition" to Marker(Position(0.0, 0.0, 0.0), Position(0.0, 0.0, 0.0)),
                "subClassPosition" to Marker(Position(0.0, 0.0, 0.0), Position(0.0, 0.0, 0.0)),
            )
        val positionData = gameSettingsManager.loadPositionData(positions, ValidPositionData::class)

        println("error: ${positionData.debug { }.getErrorOrNull()}")
        println("result: ${positionData.getOrNull()}")

        Assertions.assertEquals(true, positionData.isSuccess)
    }

    /**
     * Data class representing an imaginary gamemode's position requirements
     */
    data class ValidPositionData(
        @PositionMarker("testPosition") val testPositionMarker: Marker,
        @PositionMarker(startsWith = "testListPositions") val startWith: List<Marker>,
        @PositionMarker(endsWith = "endsWithPosition") val endsWith: Marker,
        val subClassPosition: SubPositionData,
    ) : PositionData

    /**
     * A sub class of [ValidPositionData] with nested position data
     */
    data class SubPositionData(
        @PositionMarker("subClassPosition") val innerClass: Marker,
    ) : PositionData

    data class MissingPositionData(
        val testPosition: Marker,
        val listPositions: List<Marker>,
    ) : PositionData

    data class InvalidTypePositionData(
        val testPosition: Marker,
        val listPositions: String,
    ) : PositionData

    companion object {
        @JvmStatic
        val gameSettingsManager = PositionSettingsManager()
    }
}
