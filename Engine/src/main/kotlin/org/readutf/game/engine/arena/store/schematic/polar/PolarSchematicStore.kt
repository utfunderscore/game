package org.readutf.game.engine.arena.store.schematic.polar

import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarReader
import net.hollowcube.polar.PolarWorld
import net.hollowcube.polar.PolarWriter
import net.hollowcube.schem.Rotation
import net.hollowcube.schem.Schematic
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.chunk.ChunkUtils
import org.jetbrains.annotations.Blocking
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.arena.store.schematic.ArenaSchematicStore
import org.readutf.game.engine.types.Result
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

abstract class PolarSchematicStore : ArenaSchematicStore {
    val logger = KotlinLogging.logger { }

    @Blocking
    override fun save(
        arenaId: String,
        schematic: Schematic,
        markerPositions: List<Marker>,
    ): Result<Unit> {
        val container = MinecraftServer.getInstanceManager().createInstanceContainer()

        logger.info { "Saving schematic for arena $arenaId" }
        loadChunksForPaste(container)

        logger.info { "Pasting schematic..." }
        pasteSchematic(container, schematic, markerPositions).join()

//        markerPositions.forEach {
//            container.setBlock(it.originalPosition, Block.AIR)
//        }

        val timeToSaveWorld =
            measureTimeMillis {
                val polarLoader = PolarLoader(PolarWorld())
                container.chunkLoader = polarLoader

                logger.info { "Relighting chunks..." }
                LightingChunk.relight(container, container.chunks)

                logger.info { "Saving chunks to storage..." }
                container.saveChunksToStorage().join()

                val data = PolarWriter.write(polarLoader.world())

                logger.info { "Saving data to storage..." }
                saveData(arenaId, data).mapError { return it }
            }

//        MinecraftServer
//            .getConnectionManager()
//            .onlinePlayers
//            .first()
//            .setInstance(container)

        logger.info { "Generated & Saved world in $timeToSaveWorld ms" }

        return Result.success(Unit)
    }

    override fun load(arenaId: String): Result<Instance> {
        val (data, time) =
            measureTimedValue {
                loadData(arenaId).mapError { return it }
            }

        logger.info { "Read arena data in ${time.inWholeMilliseconds}" }

        val (world, readTime) =
            measureTimedValue {
                PolarReader.read(data)
            }

        logger.info { "Read world in ${readTime.inWholeMilliseconds}" }

        val container = MinecraftServer.getInstanceManager().createInstanceContainer()

        val polarLoader = PolarLoader(world)

        container.setChunkSupplier(::LightingChunk)
        container.chunkLoader = polarLoader

        return Result.success(container)
    }

    protected abstract fun saveData(
        arenaId: String,
        data: ByteArray,
    ): Result<Unit>

    protected abstract fun loadData(arenaId: String): Result<ByteArray>

    @Blocking
    private fun loadChunksForPaste(container: InstanceContainer) {
        val taken =
            measureTimeMillis {
                container.setChunkSupplier(::LightingChunk)

                val tasks = mutableListOf<CompletableFuture<Chunk>>()
                ChunkUtils.forChunksInRange(0, 0, 16) { x, z -> tasks.add(container.loadChunk(x, z)) }
                CompletableFuture.allOf(*tasks.toTypedArray()).join()
            }
        logger.info { "Loaded chunks in $taken ms" }
    }

    @Blocking
    private fun pasteSchematic(
        instance: Instance,
        schematic: Schematic,
        markerPositions: List<Marker>,
    ): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()

        val taken =
            measureTimeMillis {
                schematic
                    .build(Rotation.NONE) { point, block ->
                        if (markerPositions.any { it.originalPosition == point }) {
                            return@build Block.AIR
                        }

                        return@build block
                    }.apply(instance, 0, 0, 0) { future.complete(Unit) }
            }
        logger.info { "Pasted schematic in $taken ms" }

        return future
    }
}
