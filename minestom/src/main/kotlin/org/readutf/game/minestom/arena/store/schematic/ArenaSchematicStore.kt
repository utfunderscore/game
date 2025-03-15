package org.readutf.game.minestom.arena.store.schematic

import com.github.michaelbull.result.Result
import net.hollowcube.schem.SpongeSchematic
import net.minestom.server.instance.Instance
import org.readutf.game.engine.arena.marker.Marker
import java.util.concurrent.CompletableFuture

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
        schematic: SpongeSchematic,
        markerPositions: List<Marker>,
    ): CompletableFuture<Result<Unit, Throwable>>

    /**
     * Load a unique copy of the arena's schematic as an instance
     */
    fun load(arenaId: String): Result<Instance, Throwable>
}
