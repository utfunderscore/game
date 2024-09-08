package org.readutf.game.server.world

import net.hollowcube.schem.Rotation
import net.hollowcube.schem.SchematicReader
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.instance.AddEntityToInstanceEvent
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.timer.TaskSchedule
import net.minestom.server.utils.chunk.ChunkUtils
import org.readutf.game.engine.utils.addListener
import org.readutf.game.engine.utils.pos
import java.io.File
import java.util.concurrent.CompletableFuture

class WorldManager {
    private val instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer()

    init {

        setWorldLoader()

        MinecraftServer.getGlobalEventHandler().addListener<AsyncPlayerConfigurationEvent> {
            it.spawningInstance = instanceContainer
            it.player.respawnPoint = pos(0, 40, 0)
        }
        MinecraftServer.getGlobalEventHandler().addListener<AddEntityToInstanceEvent> {
            val player = it.entity
            if (player !is Player) return@addListener

            MinecraftServer.getSchedulerManager().scheduleTask({
                val schematic = SchematicReader().read(File(System.getenv("user.dir"), "test.schem").toPath())
                schematic.build(Rotation.NONE, true).apply(player.instance, pos(0, 50, 0)) {
                    println("PASTED")
                }
                return@scheduleTask TaskSchedule.stop()
            }, TaskSchedule.seconds(2))
        }
    }

    private fun setWorldLoader() {
        instanceContainer.setGenerator { unit ->
            unit.modifier().fillHeight(0, 40, Block.STONE)
        }

        instanceContainer.setChunkSupplier(::LightingChunk)

        val chunks = mutableListOf<CompletableFuture<Chunk>>()
        ChunkUtils.forChunksInRange(0, 0, 32) { x, z -> chunks.add(instanceContainer.loadChunk(x, z)) }

        CompletableFuture.runAsync {
            CompletableFuture.allOf(*chunks.toTypedArray()).join()
            LightingChunk.relight(instanceContainer, instanceContainer.chunks)
        }
    }
}
