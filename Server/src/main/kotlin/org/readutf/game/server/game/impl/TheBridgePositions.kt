package org.readutf.game.server.game.impl

import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.settings.location.Position

class TheBridgePositions(
    @Position(name = "portal:1:1") val portal1Corner1: Marker,
    @Position(name = "portal:1:2") val portal1Corner2: Marker,
    @Position(name = "portal:2:1") val portal2Corner1: Marker,
    @Position(name = "portal:2:2") val portal2Corner2: Marker,
    @Position(startsWith = "safezone") val bridge1Corner1: Marker,
)
