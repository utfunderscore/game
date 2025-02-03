package org.readutf.game.engine.kit

import net.minestom.server.item.ItemStack
import org.readutf.game.engine.utils.distinctBySimilar

/**
 * A kit is a set of items that a player can receive.
 */
class Kit(
    val pallet: List<ItemStack> = ArrayList(),
    val items: List<ItemStack> = ArrayList(36),
) {
    constructor(items: List<ItemStack>) : this(
        pallet = items.distinctBySimilar { itemStack, itemStack2 -> itemStack.isSimilar(itemStack2) },
        items = items,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Kit) return false

        if (items != other.items) return false

        return true
    }

    override fun toString(): String = "Kit(pallet=$pallet, items=$items)"
}
