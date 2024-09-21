package org.readutf.game.server.world

import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.chunk.ChunkUtils
import org.readutf.game.engine.utils.addListener
import org.readutf.game.engine.utils.pos
import java.util.concurrent.CompletableFuture

class WorldManager {
    val instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer()

    init {

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

        MinecraftServer.getGlobalEventHandler().addListener<AsyncPlayerConfigurationEvent> {
            it.spawningInstance = instanceContainer
            it.player.respawnPoint = pos(0, 40, 0)
        }
    }
}
