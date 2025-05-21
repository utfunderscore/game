package org.readutf.game.minestom.platform

import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.trait.EntityEvent
import net.minestom.server.event.trait.InstanceEvent
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.minestom.platform.adapters.EntityEventGameAdapter
import org.readutf.game.minestom.platform.adapters.InstanceEventGameAdapter
import kotlin.reflect.KClass

object MinestomEventManager : GameEventManager() {

    val eventNodes = mutableMapOf<String, EventNode<Event>>()

    init {

        registerAdapter(EntityEvent::class, EntityEventGameAdapter())
        registerAdapter(InstanceEvent::class, InstanceEventGameAdapter())
    }

    override fun <T : Any> registerEventListener(
        game: GenericGame,
        type: KClass<T>,
        eventConsumer: (T) -> Unit,
    ) {
        val eventNode = eventNodes.getOrPut(game.gameId) {
            MinecraftServer.getGlobalEventHandler().addChild(EventNode.all(game.gameId))
        }

        val test =
            try {
                type as KClass<out Event>
            } catch (e: ClassCastException) {
                return
            }

        eventNode.addListener(test.java) {
            eventConsumer(it as T)
        }
    }

    override fun unregisterListeners(game: GenericGame) {
        val node = eventNodes.remove(game.gameId) ?: return
        MinecraftServer.getGlobalEventHandler().removeChild(node)
    }
}
