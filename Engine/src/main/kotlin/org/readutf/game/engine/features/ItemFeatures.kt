package org.readutf.game.engine.features

import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.ItemEntity
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import org.readutf.game.engine.stage.Stage
import kotlin.random.Random

fun Stage.allowDroppingItems() {
    registerListener<ItemDropEvent> {
        val player = it.player
        val droppedItem = it.itemStack

        val itemEntity = ItemEntity(droppedItem)
        itemEntity.setPickupDelay(java.time.Duration.ofSeconds(500))
        itemEntity.setInstance(player.instance)
        itemEntity.teleport(player.position.add(0.0, 1.5, 0.0))

        val velocity = player.position.direction().mul(6.0)
        itemEntity.velocity = velocity
    }
}

fun Stage.dropItemOnBlockBreak(itemFunc: (Block) -> ItemStack?) {
    registerListener<PlayerBlockBreakEvent> {
        if (it.isCancelled) return@registerListener
        val item = itemFunc.invoke(it.block)
        if (item == null || item.isAir) return@registerListener
        val itemEntity = ItemEntity(item)
        itemEntity.setInstance(it.instance, Pos(it.blockPosition.add(0.0, 0.5, 0.0)))

        val pos = it.blockPosition

        val d = 0.125
        val d1: Double = pos.blockX() + 0.5 + Random.nextDouble(-0.25, 0.25)
        val d2: Double = pos.blockY() + 0.5 + Random.nextDouble(-0.25, 0.25) - d
        val d3: Double = pos.blockZ() + 0.5 + Random.nextDouble(-0.25, 0.25)

        itemEntity.velocity = Vec(d1, d2, d3)
    }
}

fun dropItemsOnDeath(stage: Stage) {
//    stage.registerListener<GameDeathEvent> {
//        val player = it.player
//        player.inventory.itemStacks.forEach {
//            val droppedItem = it.itemStack
//
//            val itemEntity = ItemEntity(droppedItem)
//            itemEntity.setPickupDelay(java.time.Duration.ofSeconds(500))
//            itemEntity.setInstance(player.instance)
//            itemEntity.teleport(player.position.add(0.0, 0.5, 0.0))
//
//            val velocity = player.position.direction().mul(6.0)
//            itemEntity.velocity = velocity
//        }
//    }
}
