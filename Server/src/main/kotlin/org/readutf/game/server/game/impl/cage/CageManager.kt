package org.readutf.game.server.game.impl.cage

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.instance.block.Block
import org.readutf.game.engine.stage.Stage

class CageManager(
    val stage: Stage,
) {
    private val logger = KotlinLogging.logger {}
    private val offsets = mutableListOf<BlockVec>()
    private val cages = mutableListOf<BlockVec>()
    private val players = mutableListOf<Player>()

    val game = stage.game

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

    fun generateCage(
        player: Player,
        location: Point,
    ) {
        val team = game.getTeam(player.uuid) ?: return
        logger.info { "Generating cage for ${player.username} (Team: ${team.teamName})" }

        players.add(player)

        val block =
            if (team.teamName.equals("blue", true)) {
                Block.BLUE_STAINED_GLASS
            } else {
                Block.RED_STAINED_GLASS
            }

        val instance = player.instance

        offsets.forEach { instance.setBlock(location.add(it), block) }

        cages.add(BlockVec(location))
    }

    fun clearCages(instance: Instance): AbsoluteBlockBatch {
        val blockBatch = AbsoluteBlockBatch()

        cages.forEach { pos ->
            offsets.forEach { offset ->
                instance.setBlock(pos.add(offset), Block.AIR)
            }
        }

        cages.clear()
        return blockBatch
    }

    fun hasSpawned(player: Player): Boolean = players.contains(player)
}
