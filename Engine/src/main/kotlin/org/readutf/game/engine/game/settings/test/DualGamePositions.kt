package org.readutf.game.engine.game.settings.test

import org.readutf.game.engine.game.settings.location.PositionSettings
import org.readutf.game.engine.game.settings.location.PositionType
import org.readutf.game.engine.types.Position

data class DualGamePositions(
    @PositionType(name = "spawn:1") val team1Spawn: Position,
    @PositionType(name = "spawn:2") val team2Spawn: Position,
) : PositionSettings
