package org.readutf.game.minestom.selection

import net.bladehunt.kotstom.dsl.listen
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerStartDiggingEvent
import net.minestom.server.instance.Instance
import net.minestom.server.item.Material
import org.readutf.game.engine.utils.Cuboid
import org.readutf.game.minestom.utils.toPosition
import java.awt.Color
import java.util.UUID

object SelectionManager {
    private val leftSelection = mutableMapOf<UUID, Point>()
    private val leftHighlight = mutableMapOf<UUID, Entity>()
    private val rightSelection = mutableMapOf<UUID, Point>()
    private val rightHighlight = mutableMapOf<UUID, Entity>()

    init {
        MinecraftServer.getGlobalEventHandler().listen<PlayerBlockInteractEvent> { event ->
            if (event.player.itemInMainHand.material() != Material.BREEZE_ROD) {
                return@listen
            }

            event.isCancelled = true

            val player = event.player
            val blockPosition: BlockVec = event.blockPosition
            val playerUuid = player.uuid

            leftSelection[playerUuid] = blockPosition

            rightHighlight[playerUuid]?.remove()
            rightHighlight[playerUuid] = createHighlight(Color.BLUE, player.instance, Pos.fromPoint(blockPosition))
        }

        MinecraftServer.getGlobalEventHandler().listen<PlayerStartDiggingEvent> { event ->
            if (event.player.itemInMainHand.material() != Material.BREEZE_ROD) {
                return@listen
            }

            event.isCancelled = true

            val player = event.player
            val blockPosition = event.blockPosition
            val playerUuid = player.uuid

            setLeftSelection(playerUuid, blockPosition, player)
        }

        MinecraftServer.getGlobalEventHandler().listen<PlayerBlockBreakEvent> { event ->
            if (event.player.itemInMainHand.material() != Material.BREEZE_ROD) {
                return@listen
            }

            event.isCancelled = true

            val player = event.player
            val blockPosition = event.blockPosition
            val playerUuid = player.uuid

            setLeftSelection(playerUuid, blockPosition, player)
        }
    }

    private fun setLeftSelection(
        playerUuid: UUID,
        blockPosition: BlockVec,
        player: Player,
    ) {
        rightSelection[playerUuid] = blockPosition

        leftHighlight[playerUuid]?.remove()
        leftHighlight[playerUuid] = createHighlight(Color.BLUE, player.instance, Pos.fromPoint(blockPosition))
    }

    fun getSelection(playerId: UUID): Cuboid? {
        val left = leftSelection[playerId]
        val right = rightSelection[playerId]

        if (left == null || right == null) {
            return null
        }

        return Cuboid.fromPositions(left.toPosition(), right.toPosition())
    }

    private fun createHighlight(
        color: Color,
        instance: Instance,
        pos: Pos,
    ): Entity {
        val entity = Entity(EntityType.SHULKER)

        entity.isInvisible = true
        entity.isGlowing = true

        entity.setNoGravity(true)

        entity.instance = instance
        entity.teleport(pos)

        return entity
    }
}
