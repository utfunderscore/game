package org.readutf.game.engine.features

import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import org.readutf.game.engine.stage.Stage
import java.util.function.BiPredicate

fun Stage.setBlockPlaceRule(biPredicate: BiPredicate<Player, Block>) {
    registerListener<PlayerBlockPlaceEvent> {
        if (!biPredicate.test(it.player, it.block)) {
            it.isCancelled = true
        }
    }
}

fun Stage.setBlockBreakRule(biPredicate: BiPredicate<Player, Block>) {
    registerListener<PlayerBlockBreakEvent> {
        if (!biPredicate.test(it.player, it.block)) {
            it.isCancelled = true
        }
    }
}
