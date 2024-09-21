package org.readutf.game.engine.settings

import org.junit.jupiter.api.Test
import org.readutf.game.engine.settings.location.PositionSettings
import org.readutf.game.engine.types.Position
import java.io.File

class TestPositionRequirements {
    private val gameSettingsManager = GameSettingsManager(File(""))

    @Test
    fun generatePositionRequirements() {
        println(gameSettingsManager.generatePositionRequirements(PositionRequirements::class))
    }

    data class PositionRequirements(
        val testPosition: Position,
        val listPositions: List<Position>,
    ) : PositionSettings
}
