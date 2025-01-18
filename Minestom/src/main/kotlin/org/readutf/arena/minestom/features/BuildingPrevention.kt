package org.readutf.arena.minestom.features

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import org.readutf.game.engine.stage.GenericStage

fun GenericStage.setBlockPlaceRule(blockRule: BlockRule) {
    registerListener<PlayerBlockPlaceEvent> {
        if (it.isCancelled) return@registerListener
        if (!blockRule.allow(it.player, it.block, it.blockPosition)) {
            it.isCancelled = true
        }
    }
}

fun GenericStage.setBlockBreakRule(blockRule: BlockRule) {
    registerListener<PlayerBlockBreakEvent> {
        if (it.isCancelled) return@registerListener

        if (!blockRule.allow(it.player, it.block, it.blockPosition)) {
            it.isCancelled = true
        }
    }
}

fun interface BlockRule {
    fun allow(
        player: Player,
        block: Block,
        position: BlockVec,
    ): Boolean
}
