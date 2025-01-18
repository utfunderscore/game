package org.readutf.arena.minestom.arena.store.polar

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import net.hollowcube.polar.PolarLoader
import net.hollowcube.polar.PolarReader
import net.hollowcube.polar.PolarWorld
import net.hollowcube.polar.PolarWriter
import net.hollowcube.schem.Rotation
import net.hollowcube.schem.Schematic
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.ChunkRange
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import org.jetbrains.annotations.Blocking
import org.readutf.arena.minestom.platform.MinestomWorld
import org.readutf.arena.minestom.platform.schematic.MinestomSchematic
import org.readutf.game.engine.arena.marker.Marker
import org.readutf.game.engine.arena.schematic.ArenaSchematicStore
import org.readutf.game.engine.platform.schematic.ArenaSchematic
import org.readutf.game.engine.utils.SResult
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis
import kotlin.time.measureTimedValue

abstract class PolarSchematicStore : ArenaSchematicStore<MinestomWorld> {
    val logger = KotlinLogging.logger { }

    @Blocking
    override fun save(
        arenaId: String,
        schematic: ArenaSchematic,
        markerPositions: List<Marker>,
    ): SResult<Unit> {
        if (schematic !is MinestomSchematic) {
            return Err("Invalid schematic type")
        }

        val container = MinecraftServer.getInstanceManager().createInstanceContainer()

        logger.info { "Saving schematic for arena $arenaId" }
        loadChunksForPaste(container)

        logger.info { "Pasting schematic..." }
        pasteSchematic(container, schematic.schematic, markerPositions).join()

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
                saveData(arenaId, data).getOrElse { return Err(it) }
            }

//        MinecraftServer
//            .getConnectionManager()
//            .onlinePlayers
//            .first()
//            .setInstance(container)

        logger.info { "Generated & Saved world in $timeToSaveWorld ms" }

        return Ok(Unit)
    }

    override fun load(arenaId: String): SResult<MinestomWorld> {
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

        val container = MinecraftServer.getInstanceManager().createInstanceContainer()

        val polarLoader = PolarLoader(world)

        container.setChunkSupplier(::LightingChunk)
        container.chunkLoader = polarLoader

        return Ok(MinestomWorld(container))
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
        schematic: Schematic,
        markerPositions: List<Marker>,
    ): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()

        val taken =
            measureTimeMillis {
//                schematic
//                    .apply(Rotation.NONE) { point, block ->
//                        if (markerPositions.any { it.originalPosition == point }) {
//                            return@build Block.AIR
//                        }
//
//                        return@build block
//                    }.apply(instance, 0, 0, 0) { future.complete(Unit) }

                schematic.createBatch(Rotation.NONE)
                    .apply(instance, 0, 0, 0) { future.complete(Unit) }
            }
        logger.info { "Pasted schematic in $taken ms" }

        return future
    }
}
