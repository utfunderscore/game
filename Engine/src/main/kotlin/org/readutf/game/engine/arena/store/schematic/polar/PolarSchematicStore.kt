package org.readutf.game.engine.arena.store.schematic.polar

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
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
import net.minestom.server.utils.chunk.ChunkUtils
import org.readutf.game.engine.arena.store.schematic.ArenaSchematicStore
import org.readutf.game.engine.types.Result
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

abstract class PolarSchematicStore : ArenaSchematicStore {
    private val logger = KotlinLogging.logger { }

    override suspend fun save(
        arenaId: String,
        schematic: Schematic,
    ): Result<Unit> {
        val container = MinecraftServer.getInstanceManager().createInstanceContainer()

        logger.info { "Saving schematic for arena $arenaId" }
        loadChunksForPaste(container)

        logger.info { "Pasting schematic..." }
        pasteSchematic(container, schematic)

        val timeToSaveWorld =
            measureTimeMillis {
                val polarLoader = PolarLoader(PolarWorld())
                container.chunkLoader = polarLoader

                logger.info { "Saving chunks to storage..." }
                container.saveChunksToStorage().await()

                val data = PolarWriter.write(polarLoader.world())

                logger.info { "Saving data to storage..." }
                saveData(arenaId, data).onFailure {
                    return Result.failure(it)
                }
            }

        logger.info { "Generated & Saved world in $timeToSaveWorld ms" }

        return Result.success(Unit)
    }

    override suspend fun load(arenaId: String): Result<Instance> {
        val (data, time) =
            measureTimedValue {
                loadData(arenaId).onFailure {
                    return Result.failure(it)
                }
            }

        logger.info { "Read arena data in ${time.inWholeMilliseconds}" }

        val (world, readTime) =
            measureTimedValue {
                PolarReader.read(data)
            }

        logger.info { "Read world in ${readTime.inWholeMilliseconds}" }

        val container = MinecraftServer.getInstanceManager().createInstanceContainer()

        val polarLoader = PolarLoader(world)

        container.chunkLoader = polarLoader

        return Result.success(container)
    }

    protected abstract fun saveData(
        arenaId: String,
        data: ByteArray,
    ): Result<Unit>

    protected abstract fun loadData(arenaId: String): Result<ByteArray>

    private suspend fun loadChunksForPaste(container: InstanceContainer) {
        val taken =
            measureTimeMillis {
                val tasks = mutableListOf<CompletableFuture<Chunk>>()
                println("thread: ${Thread.currentThread().name}")
                ChunkUtils.forChunksInRange(0, 0, 16) { x, z -> tasks.add(container.loadChunk(x, z)) }
                CompletableFuture.allOf(*tasks.toTypedArray()).await()
            }
        logger.info { "Loaded chunks in $taken ms" }
    }

    private suspend fun pasteSchematic(
        instance: Instance,
        schematic: Schematic,
    ) {
        val taken =
            measureTimeMillis {
                CompletableFuture<Unit>()
                println("thread: ${Thread.currentThread().name}")

                schematic.build(Rotation.NONE) { it }.apply(instance, 0, 0, 0) {}
            }
        logger.info { "Pasted schematic in $taken ms" }
    }
}
