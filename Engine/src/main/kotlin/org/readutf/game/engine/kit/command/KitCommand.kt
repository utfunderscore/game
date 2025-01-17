package org.readutf.game.engine.kit.command

import com.github.michaelbull.result.getOrElse
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.platform.Platform
import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.platform.player.ArenaPlayer
import org.readutf.game.engine.utils.toComponent

class KitCommand<T : ArenaItemStack<T>>(
    val platform: Platform<T>,
    private val kitManager: KitManager<T>,
) {

    fun save(
        player: ArenaPlayer<T>,
        name: String,
    ) {
        kitManager.saveKit(name, Kit(player.getInventory() + player.getArmor()))
        player.sendMessage("&aSaved kit $name".toComponent())
    }

    fun preview(
        player: ArenaPlayer<T>,
        name: String,
    ) {
        val kit = kitManager.loadKit(name).getOrElse {
            player.sendMessage("&cKit $name not found".toComponent())
            return
        }

        val inventory = kit.items.subList(0, kit.items.size - 4)
        val armor = kit.items.subList(kit.items.size - 4, kit.items.size)

        player.setInventory(inventory)
        player.setArmor(armor)

        player.sendMessage("&aPreviewing kit $name".toComponent())
    }
}
