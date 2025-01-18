package org.readutf.arena.minestom.platform

import net.kyori.adventure.text.Component
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import org.readutf.game.engine.platform.player.ArenaPlayer
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.utils.Position

class MinestomPlayer(
    val player: Player,
) : ArenaPlayer<MinestomItemStack>(
    player.uuid,
) {
    override fun sendMessage(message: Component) {
        player.sendMessage(message)
    }

    override fun getName(): String = player.username

    override fun teleport(position: Position, gameWorld: ArenaWorld) {
        val instance = (gameWorld as MinestomWorld).instance
        if (player.instance == instance) {
            player.teleport(Pos(position.x, position.y, position.z))
        } else {
            player.setInstance(instance, Pos(position.x, position.y, position.z))
        }
    }

    override fun teleport(position: Position) {
        player.teleport(Pos(position.x, position.y, position.z))
    }

    override fun getInventory(): List<MinestomItemStack> = player.inventory.itemStacks.toList().subList(0, 36).map { MinestomItemStack(it) }

    override fun getArmor(): List<MinestomItemStack> = player.inventory.itemStacks.toList().subList(36, 40).map { MinestomItemStack(it) }

    override fun setArmor(items: List<MinestomItemStack>) {
        items.forEachIndexed { index, minestomItemStack ->
            player.inventory.setItemStack(index + 36, minestomItemStack.itemStack)
        }
    }

    override fun setInventory(items: List<MinestomItemStack>) {
        items.forEachIndexed { index, minestomItemStack ->
            player.inventory.setItemStack(index, minestomItemStack.itemStack)
        }
    }
}
