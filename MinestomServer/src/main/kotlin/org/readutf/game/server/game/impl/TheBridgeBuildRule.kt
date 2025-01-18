package org.readutf.game.server.game.impl

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import org.readutf.arena.minestom.features.BlockRule
import org.readutf.arena.minestom.platform.toPosition
import org.readutf.game.engine.utils.Cuboid

class TheBridgeBuildRule(
    private val stage: TheBridgeStage,
    private val safeZones: Collection<Cuboid>,
) : BlockRule {
    override fun allow(
        player: Player,
        block: Block,
        position: BlockVec,
    ): Boolean {
        if (safeZones.any { it.contains(position.toPosition()) }) {
            return false
        }

        if (block != Block.BLUE_TERRACOTTA && block != Block.RED_TERRACOTTA) {
            return false
        }

        if (!stage.hasCageDropped) {
            return false
        }

        return true
    }
}
