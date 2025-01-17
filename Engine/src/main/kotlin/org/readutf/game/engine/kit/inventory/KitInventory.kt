package org.readutf.game.engine.kit.inventory

import org.readutf.game.engine.platform.item.ArenaItemStack

data class KitInventory<T : ArenaItemStack<T>>(
    val items: List<T> = ArrayList(36),
    val armorItems: List<T> = ArrayList(4),
)
