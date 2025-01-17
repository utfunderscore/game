package org.readutf.game.server.game.dual

import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.MarkerPosition
import org.readutf.game.engine.settings.location.PositionData

open class DualGamePositions(
    @MarkerPosition(name = "spawn:1") open val team1Spawn: Marker,
    @MarkerPosition(name = "spawn:2") open val team2Spawn: Marker,
) : PositionData
