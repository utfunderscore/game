package org.readutf.game.engine.settings

import net.minestom.server.coordinate.Vec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.Position
import org.readutf.game.engine.settings.location.PositionData

class TestValidPositionData {
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
                "testPosition" to Marker(Vec(0.0, 0.0, 0.0), Vec(0.0, 0.0, 0.0)),
                "testListPositions" to Marker(Vec(0.0, 0.0, 0.0), Vec(0.0, 0.0, 0.0)),
                "endsWithPosition" to Marker(Vec(0.0, 0.0, 0.0), Vec(0.0, 0.0, 0.0)),
                "subClassPosition" to Marker(Vec(0.0, 0.0, 0.0), Vec(0.0, 0.0, 0.0)),
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
        @Position("testPosition") val testPosition: Marker,
        @Position(startsWith = "testListPositions") val startWith: List<Marker>,
        @Position(endsWith = "endsWithPosition") val endsWith: Marker,
        val subClassPosition: SubPositionData,
    ) : PositionData

    /**
     * A sub class of [ValidPositionData] with nested position data
     */
    data class SubPositionData(
        @Position("subClassPosition") val innerClass: Marker,
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
        @JvmStatic val gameSettingsManager = PositionSettingsManager()
    }
}
