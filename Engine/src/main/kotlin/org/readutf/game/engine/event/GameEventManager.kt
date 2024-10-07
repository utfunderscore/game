package org.readutf.game.engine.event

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.entity.EntityDamageEvent
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

    private val registeredTypes = mutableSetOf<KClass<out Event>>()
    private val noAdapters = mutableSetOf<KClass<out Event>>()
    private val registeredListeners = LinkedHashMap<Game<*>, LinkedHashMap<KClass<out Event>, MutableList<GameListener>>>()
    private val eventStackTraceEnabled = mutableSetOf<KClass<out Event>>()

    init {
        eventStackTraceEnabled.add(EntityDamageEvent::class)
    }

    private fun eventHandler(event: Event) {
        val eventType = event::class
        val adapter = findAdapter(event)

        if (adapter == null) {
            if (!noAdapters.contains(eventType)) {
                noAdapters.add(eventType)
                logger.info { "No event adapter found for event type: $eventType" }
            }
            return
        }
        val game = adapter.convert(event) ?: return

        callEvent(event, game)
    }

    fun registerListener(
        game: Game<*>,
        kClass: KClass<out Event>,
        listener: GameListener,
    ) {
        if (!registeredTypes.contains(kClass)) {
            registeredTypes.add(kClass)
            println("Registering listener for event type: $kClass")
            MinecraftServer.getGlobalEventHandler().addListener(kClass.java) {
                eventHandler(it)
            }
        }

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

    fun <T : Event> callEvent(
        event: T,
        game: Game<*>,
    ): T {
        val gameListeners = registeredListeners[game]
        if (gameListeners == null) {
            if (eventStackTraceEnabled.contains(event::class)) {
                logger.info { "No listeners found for game: $game" }
            }

            return event
        }

        val listeners = gameListeners[event::class]
        if (listeners == null) {
            if (eventStackTraceEnabled.contains(event::class)) {
                logger.info { "No listeners found for event type: ${event::class}" }
            }
            return event
        }

        listeners.forEach {
            try {
                it.onEvent(event)
            } catch (e: Exception) {
                logger.error(e) { "Error occurred while calling event listener" }
            }
        }
        return event
    }
}
