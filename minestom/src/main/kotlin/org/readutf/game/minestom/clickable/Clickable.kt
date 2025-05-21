package org.readutf.game.minestom.clickable

import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import java.util.*

abstract class Clickable(
    val id: UUID = UUID.randomUUID(),
    val refreshOnClick: Boolean = true,
) {
    abstract fun getItemStack(): ItemStack

    /**
     * @param player the player who clicked
     * @param clickType true if right click, false if left click
     */
    abstract fun onClick(
        player: Player,
        clickType: Boolean,
    )
}
