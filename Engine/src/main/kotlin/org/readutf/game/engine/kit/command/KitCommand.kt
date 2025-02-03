package org.readutf.game.engine.kit.command

import net.minestom.server.entity.Player
import org.readutf.game.engine.kit.Kit
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.utils.toComponent
import revxrsal.commands.annotation.Command

class KitCommand(
    val kitManager: KitManager,
) {
    @Command("kit save <name>")
    fun save(
        player: Player,
        name: String,
    ) {
        kitManager.saveKit(name, Kit(player.inventory.itemStacks.toList()))
        player.sendMessage("&aSaved kit $name".toComponent())
    }

    @Command("kit preview <name>")
    fun preview(
        player: Player,
        name: String,
    ) {
        val kit =
            kitManager.loadKit(name).getOrNull() ?: run {
                player.sendMessage("&cKit $name not found".toComponent())
                return
            }
        kit.items.forEachIndexed { index, item -> player.inventory.setItemStack(index, item) }

        player.sendMessage("&aPreviewing kit $name".toComponent())
    }
}
