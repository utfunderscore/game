package org.readutf.game.server.game.impl

import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import java.util.function.BiPredicate

object TheBridgeBuildRule : BiPredicate<Player, Block> {
    override fun test(
        player: Player,
        block: Block,
    ): Boolean = block == Block.RED_TERRACOTTA || block == Block.BLUE_TERRACOTTA
}
