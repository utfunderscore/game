package org.readutf.game.engine.platform.player

import net.kyori.adventure.text.Component
import org.readutf.game.engine.platform.item.ArenaItemStack
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.utils.Position
import java.util.UUID

typealias GamePlayer = ArenaPlayer<*>

abstract class ArenaPlayer<ITEM : ArenaItemStack<ITEM>>(val uuid: UUID) {

    open fun sendMessage(message: String) {
        sendMessage(Component.text(message))
    }

    abstract fun sendMessage(message: Component)

    abstract fun teleport(position: Position, gameWorld: ArenaWorld)

    abstract fun getInventory(): List<ITEM>

    abstract fun getArmor(): List<ITEM>

    abstract fun setInventory(items: List<ITEM>)

    abstract fun setArmor(items: List<ITEM>)
}
