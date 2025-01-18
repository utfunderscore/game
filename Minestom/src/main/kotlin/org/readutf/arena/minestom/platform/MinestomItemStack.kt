package org.readutf.arena.minestom.platform

import net.minestom.server.item.ItemStack
import org.readutf.game.engine.platform.item.ArenaItemStack

class MinestomItemStack(
    val itemStack: ItemStack,
) : ArenaItemStack<MinestomItemStack> {

    override fun isSimilar(itemStack: MinestomItemStack): Boolean = this.itemStack.isSimilar(itemStack.itemStack)

    override fun withAmount(toInt: Int): MinestomItemStack = MinestomItemStack(itemStack.withAmount(toInt))

    override fun getAmount(): Int = itemStack.amount()

    override fun isAir(): Boolean = itemStack.isAir
}
