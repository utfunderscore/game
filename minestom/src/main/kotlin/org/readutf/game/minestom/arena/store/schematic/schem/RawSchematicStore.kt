package org.readutf.game.minestom.arena.store.schematic.schem

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.runCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.SpongeSchematic
import net.hollowcube.schem.reader.SpongeSchematicReader
import net.hollowcube.schem.writer.SpongeSchematicWriter
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.ChunkRange
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import org.jetbrains.annotations.Blocking
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.minestom.arena.store.schematic.ArenaSchematicStore
import java.util.concurrent.CompletableFuture

abstract class RawSchematicStore : ArenaSchematicStore {
    private val logger = KotlinLogging.logger { }

    override fun save(
        arenaId: String,
        schematic: SpongeSchematic,
        markerPositions: List<Marker>,
    ): CompletableFuture<Result<Unit, Throwable>> = CompletableFuture.supplyAsync {
        runCatching {
            SpongeSchematicWriter().write(schematic)
        }.andThen {
            saveData(arenaId, it)
        }
    }

    @Blocking
    override fun load(arenaId: String): Result<Instance, Throwable> {
        val data = loadData(arenaId).getOrElse { return Err(it) }
        val schematic =
            try {
                SpongeSchematicReader().read(data)
            } catch (e: Exception) {
                logger.error(e) { }
                return Err(e)
            }

        val instance = MinecraftServer.getInstanceManager().createInstanceContainer()
        instance.setChunkSupplier(::LightingChunk)

        val pasteFuture = CompletableFuture<Unit>()

        val chunks = mutableListOf<CompletableFuture<Chunk>>()
        ChunkRange.chunksInRange(
            0,
            0,
            8,
        ) { x: Int, z: Int -> chunks.add(instance.loadChunk(x, z)) }

        CompletableFuture
            .allOf(
                *chunks.toTypedArray(),
            ).join()

        schematic.createBatch { it }.apply(instance, 0, 0, 0) {
            pasteFuture.complete(Unit)
        }

        pasteFuture.join()

        return Ok(instance)
    }

    abstract fun saveData(
        arenaId: String,
        data: ByteArray,
    ): Result<Unit, Throwable>

    abstract fun loadData(arenaId: String): Result<ByteArray, Throwable>
}
