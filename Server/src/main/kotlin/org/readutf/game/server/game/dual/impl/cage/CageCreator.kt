package org.readutf.game.server.game.dual.impl.cage

import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.instance.block.Block
import org.readutf.game.engine.Game
import org.readutf.game.server.game.dual.utils.DualArena

interface CageCreator {
    fun createCage(
        player: Player,
        location: Point,
        game: Game<out DualArena>,
    ): AbsoluteBlockBatch

    class DefaultCageCreator : CageCreator {
        val offsets = mutableListOf<BlockVec>()

        init {

            for (x in -2 until 3) {
                for (z in -2 until 3) {
                    offsets.add(BlockVec(x, -1, z))
                    offsets.add(BlockVec(x, 3, z))
                    if (x == -2 || x == 2 || z == -2 || z == 2) {
                        offsets.add(BlockVec(x, 0, z))
                        offsets.add(BlockVec(x, 1, z))
                        offsets.add(BlockVec(x, 2, z))
                    }
                }
            }
        }

        override fun createCage(
            player: Player,
            location: Point,
            game: Game<out DualArena>,
        ): AbsoluteBlockBatch {
            val teamName: String? = game.getTeam(player.uuid)?.gameName

            val block =
                if (teamName.equals("red", true)) {
                    Block.RED_STAINED_GLASS
                } else {
                    Block.BLUE_STAINED_GLASS
                }

            val batch = AbsoluteBlockBatch()

            offsets.forEach {
                batch.setBlock(location.add(it), block)
            }

            return batch
        }
    }
}
