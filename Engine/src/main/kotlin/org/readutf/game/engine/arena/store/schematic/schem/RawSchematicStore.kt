package org.readutf.game.engine.arena.store.schematic.schem

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.schem.Rotation
import net.hollowcube.schem.Schematic
import net.hollowcube.schem.SchematicReader
import net.hollowcube.schem.SchematicWriter
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.utils.chunk.ChunkUtils
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.arena.store.schematic.ArenaSchematicStore
import org.readutf.game.engine.types.Result
import java.io.ByteArrayInputStream
import java.util.concurrent.CompletableFuture

abstract class RawSchematicStore : ArenaSchematicStore {
    private val logger = KotlinLogging.logger { }

    override fun save(
        arenaId: String,
        schematic: Schematic,
        markerPositions: List<Marker>,
    ): Result<Unit> {
        val data =
            try {
                SchematicWriter().write(schematic)
            } catch (e: Exception) {
                logger.error(e) { }
                return Result.failure("Failed to write schematic to store")
            }

        return saveData(arenaId, data)
    }

    override fun load(arenaId: String): Result<Instance> {
        val data = loadData(arenaId).mapError { return it }
        val schematic =
            try {
                SchematicReader().read(ByteArrayInputStream(data))
            } catch (e: Exception) {
                logger.error(e) { }
                return Result.failure("Failed to read schematic from store")
            }

        val instance = MinecraftServer.getInstanceManager().createInstanceContainer()
        instance.setChunkSupplier(::LightingChunk)

        val pasteFuture = CompletableFuture<Unit>()

        val chunks = mutableListOf<CompletableFuture<Chunk>>()
        ChunkUtils.forChunksInRange(
            0,
            0,
            8,
        ) { x: Int, z: Int -> chunks.add(instance.loadChunk(x, z)) }

        CompletableFuture
            .allOf(
                *chunks.toTypedArray(),
            ).join()

        schematic.build(Rotation.NONE, true).applyUnsafe(instance, 0, 0, 0) {
            pasteFuture.complete(Unit)
        }
        pasteFuture.join()
//
//        LightingChunk.relight(instance, instance.chunks)

        return Result.success(instance)
    }

    abstract fun saveData(
        arenaId: String,
        data: ByteArray,
    ): Result<Unit>

    abstract fun loadData(arenaId: String): Result<ByteArray>
}
