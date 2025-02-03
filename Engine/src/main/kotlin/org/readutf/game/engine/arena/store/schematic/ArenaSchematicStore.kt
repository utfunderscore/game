package org.readutf.game.engine.arena.store.schematic

import net.hollowcube.schem.Schematic
import net.minestom.server.instance.Instance
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.types.Result

interface ArenaSchematicStore {
    /**
     * Store a copy of the temporary instance used to
     * contain the pre-pasted schematic
     *
     * It is the responsibility of the store to remove marker signs,
     * and handle freeing the instance when it is no longer needed
     */
    fun save(
        arenaId: String,
        schematic: Schematic,
        markerPositions: List<Marker>,
    ): Result<Unit>

    /**
     * Load a unique copy of the arena's schematic as an instance
     */
    fun load(arenaId: String): Result<Instance>
}
