package org.readutf.game.engine.event

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.trait.EntityEvent
import org.readutf.game.engine.Game
import org.readutf.game.engine.event.adapter.EventAdapter
import org.readutf.game.engine.event.adapter.impl.EntityEventAdapter
import org.readutf.game.engine.event.adapter.impl.GameEventAdapter
import org.readutf.game.engine.event.adapter.impl.StageEventAdapter
import org.readutf.game.engine.event.impl.StageEvent
import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.utils.addListener
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Sits on top of the Minestom event system and allows for events
 * to be assigned to a given active game instance. Adapters for each event type
 * can be registered, including generic ones for abstract events like `PlayerEvent`.
 */
object GameEventManager {
    private val logger = KotlinLogging.logger { }

    private val eventFilters: Map<KClass<out Event>, EventAdapter> =
        mapOf(
            StageEvent::class to StageEventAdapter(),
            EntityEvent::class to EntityEventAdapter(),
            GameEvent::class to GameEventAdapter(),
        )
    private val noAdapters = mutableSetOf<KClass<out Event>>()
    private val registeredListeners = LinkedHashMap<Game<*>, LinkedHashMap<KClass<out Event>, MutableList<GameListener>>>()
    private val eventStackTraceEnabled = mutableSetOf<KClass<out Event>>()

    init {
        MinecraftServer.getGlobalEventHandler().addChild(createEventNode())
    }

    private fun createEventNode(): EventNode<Event> {
        val node =
            EventNode
                .all("game")

        node.addListener<Event> { event ->
            val eventType = event::class
            val adapter = findAdapter(event)
            if (adapter == null) {
                if (!noAdapters.contains(eventType)) {
                    noAdapters.add(eventType)
                    logger.debug { "No event adapter found for event type: $eventType" }
                }
                return@addListener
            }
            val game = adapter.convert(event) ?: return@addListener

            callEvent(event, game)
        }
        return node
    }

    fun registerEvent(
        game: Game<*>,
        kClass: KClass<out Event>,
        listener: GameListener,
    ) {
        val listeners =
            registeredListeners
                .getOrPut(game) { LinkedHashMap() } // Game Listeners
                .getOrPut(kClass) { mutableListOf() } // EventType listeners

        listeners.add(listener)
    }

    fun unregisterEvent(
        game: Game<*>,
        kClass: KClass<out Event>,
        listener: GameListener,
    ) {
        val listeners = registeredListeners[game]?.get(kClass) ?: return
        listeners.remove(listener)
    }

    private fun findAdapter(event: Event): EventAdapter? {
        synchronized(this) {
            val byExact = eventFilters[event::class]
            if (byExact != null) return byExact

            return eventFilters.filterKeys { kClass -> event::class.isSubclassOf(kClass) }.values.firstOrNull()
        }
    }

    fun callEvent(
        event: Event,
        game: Game<*>,
    ) = registeredListeners[game]?.get(event::class)?.forEach { listener ->
        listener.onEvent(event)
    }
}
