package org.readutf.game.minestom.arena.store.schematic.polar

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarReader
import net.hollowcube.polar.PolarWorld
import net.hollowcube.polar.PolarWriter
import net.hollowcube.schem.SpongeSchematic
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.ChunkRange
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import org.jetbrains.annotations.Blocking
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.utils.SResult
import org.readutf.game.minestom.arena.marker.MarkerUtils
import org.readutf.game.minestom.arena.store.schematic.ArenaSchematicStore
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

abstract class PolarSchematicStore : ArenaSchematicStore {
    val logger = KotlinLogging.logger { }

    @Blocking
    override fun save(
        arenaId: String,
        schematic: SpongeSchematic,
        markerPositions: List<Marker>,
    ): CompletableFuture<SResult<Unit>> {
        val container = MinecraftServer.getInstanceManager().createInstanceContainer()

        logger.info { "Saving schematic for arena $arenaId" }
        loadChunksForPaste(container)

        return pasteSchematic(container, schematic, markerPositions)
            .thenCompose {
                logger.info { "Saving chunks to storage..." }
                val polarLoader = PolarLoader(PolarWorld())
                container.chunkLoader = polarLoader
                container.setChunkSupplier(::LightingChunk)

                return@thenCompose container.saveChunksToStorage().thenApply {
                    logger.info { "Relighting chunks..." }
                    LightingChunk.relight(container, container.chunks)

                    logger.info { "Saving data to storage..." }
                    val data = PolarWriter.write(polarLoader.world())

                    return@thenApply saveData(arenaId, data)
                }
            }
    }

    override fun load(arenaId: String): SResult<Instance> {
        logger.info { "Loading arena $arenaId" }

        val (data, time) =
            measureTimedValue {
                loadData(arenaId).getOrElse { return Err(it) }
            }

        logger.info { "Read arena data in ${time.inWholeMilliseconds}" }

        val (world, readTime) =
            measureTimedValue {
                PolarReader.read(data)
            }

        logger.info { "Read world in ${readTime.inWholeMilliseconds}" }

        val instance = MinecraftServer.getInstanceManager().createInstanceContainer()

        val polarLoader = PolarLoader(world)

        instance.setChunkSupplier(::LightingChunk)
        instance.chunkLoader = polarLoader
        ChunkRange.chunksInRange(0, 0, 16) { x, z -> instance.loadChunk(x, z) }
        LightingChunk.relight(instance, instance.chunks)

        instance.time = 6000
        instance.timeRate = 0

        return Ok(instance)
    }

    protected abstract fun saveData(
        arenaId: String,
        data: ByteArray,
    ): SResult<Unit>

    protected abstract fun loadData(arenaId: String): SResult<ByteArray>

    @Blocking
    private fun loadChunksForPaste(container: InstanceContainer) {
        val taken =
            measureTimeMillis {
                container.setChunkSupplier(::LightingChunk)

                val tasks = mutableListOf<CompletableFuture<Chunk>>()
                ChunkRange.chunksInRange(0, 0, 16) { x, z -> tasks.add(container.loadChunk(x, z)) }
                CompletableFuture.allOf(*tasks.toTypedArray()).join()
            }
        logger.info { "Loaded chunks in $taken ms" }
    }

    @Blocking
    private fun pasteSchematic(
        instance: Instance,
        schematic: SpongeSchematic,
        markerPositions: List<Marker>,
    ): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()

        /**
         * Replace all sign blocks with air
         */

        val offset = schematic.offset

        logger.info { "Pasting schematic at $offset..." }

        CompletableFuture.runAsync {
            schematic
                .createBatch { block ->
                    if (block?.nbt() != null) {
                        logger.info { "Block: ${block.nbt()}" }
                    }

                    if (block != null && MarkerUtils.isMarker(block)) {
                        Block.AIR
                    } else {
                        block
                    }
                }.apply(instance, 0, 0, 0) {
                    future.complete(Unit)
                }
        }

        return future
    }
}
