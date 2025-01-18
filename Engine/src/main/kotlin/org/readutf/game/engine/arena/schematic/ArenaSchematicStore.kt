package org.readutf.game.engine.arena.schematic

import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.platform.schematic.ArenaSchematic
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.utils.SResult

interface ArenaSchematicStore<WORLD : ArenaWorld> {
    /**
     * Store a copy of the temporary instance used to
     * contain the pre-pasted schematic
     *
     * It is the responsibility of the store to remove marker signs,
     * and handle freeing the instance when it is no longer needed
     */
    fun save(
        arenaId: String,
        schematic: ArenaSchematic,
        markerPositions: List<Marker>,
    ): SResult<Unit>

    /**
     * Load a unique copy of the arena's schematic as an instance
     */
    fun load(arenaId: String): SResult<WORLD>
}
