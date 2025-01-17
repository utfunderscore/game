package org.readutf.game.engine.kit

import org.readutf.game.engine.platform.item.ArenaItemStack

/**
 * A kit is a set of items that a player can receive.
 */
class Kit<T : ArenaItemStack<T>>(
    val pallet: List<T> = ArrayList(),
    val items: List<T> = ArrayList(36),
) {
    constructor(items: List<T>) : this(
        pallet = items.distinct(),
        items = items,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Kit<*>) return false

        if (items != other.items) return false

        return true
    }

    override fun toString(): String = "Kit(pallet=$pallet, items=$items)"
    override fun hashCode(): Int = javaClass.hashCode()
}
