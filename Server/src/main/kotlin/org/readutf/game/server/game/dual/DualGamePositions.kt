package org.readutf.game.server.game.dual

import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.Position
import org.readutf.game.engine.settings.location.PositionData

open class DualGamePositions(
    @Position(name = "spawn:1") open val team1Spawn: Marker,
    @Position(name = "spawn:2") open val team2Spawn: Marker,
) : PositionData
