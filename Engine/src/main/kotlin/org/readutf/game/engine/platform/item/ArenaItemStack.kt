package org.readutf.game.engine.platform.item

interface ArenaItemStack<SELF : ArenaItemStack<SELF>> : Comparable<SELF> {
    fun isSimilar(itemStack: SELF): Boolean
    fun withAmount(toInt: Int): SELF
    fun getAmount(): Int
    fun isAir(): Boolean

    override fun compareTo(other: SELF): Int = if (isSimilar(other)) 0 else 1
}
