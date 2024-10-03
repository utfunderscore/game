package org.readutf.game.engine.settings.test

import net.minestom.server.coordinate.Vec
import org.readutf.game.engine.settings.location.PositionSettings
import org.readutf.game.engine.settings.location.PositionType

data class DualGamePositions(
    @PositionType(name = "spawn:1") val team1Spawn: Vec,
    @PositionType(name = "spawn:2") val team2Spawn: Vec,
) : PositionSettings
