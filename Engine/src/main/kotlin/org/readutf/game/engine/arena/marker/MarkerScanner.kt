package org.readutf.game.engine.arena.marker

import org.readutf.game.engine.platform.schematic.ArenaSchematic

interface MarkerScanner {

    fun getMarkerPositions(schematic: ArenaSchematic): Map<String, Marker>
}
