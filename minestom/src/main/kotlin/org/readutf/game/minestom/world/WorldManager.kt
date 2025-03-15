package org.readutf.game.minestom.world

import net.bladehunt.kotstom.dsl.listen
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.ChunkRange
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.Chunk
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import java.util.concurrent.CompletableFuture

object WorldManager {
    private val instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer()

    init {

        instanceContainer.setGenerator { unit ->
            unit.modifier().fillHeight(0, 40, Block.STONE)
        }

        instanceContainer.enableAutoChunkLoad(true)

        instanceContainer.setChunkSupplier(::LightingChunk)

        val chunks = mutableListOf<CompletableFuture<Chunk>>()
        ChunkRange.chunksInRange(0, 0, 3) { x, z -> chunks.add(instanceContainer.loadChunk(x, z)) }

        CompletableFuture.runAsync {
            CompletableFuture.allOf(*chunks.toTypedArray()).join()
            LightingChunk.relight(instanceContainer, instanceContainer.chunks)
        }

        MinecraftServer.getGlobalEventHandler().listen<AsyncPlayerConfigurationEvent> { event ->
            event.spawningInstance = instanceContainer
            event.player.respawnPoint = Pos(0.5, 40.0, 0.5)
        }
    }
}
