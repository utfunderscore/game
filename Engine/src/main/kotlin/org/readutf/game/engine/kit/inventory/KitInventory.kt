package org.readutf.game.engine.kit.inventory

import net.minestom.server.item.ItemStack

data class KitInventory(
    val items: List<ItemStack> = ArrayList(36),
    val armorItems: List<ItemStack> = ArrayList(4),
)
