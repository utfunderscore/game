package org.readutf.arena.minestom.features

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
