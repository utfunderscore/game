package org.readutf.game.engine.event

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventGameAdapter
import org.readutf.game.engine.event.adapter.game.GameEventGameAdapter
import org.readutf.game.engine.event.adapter.game.StageEventGameAdapter
import org.readutf.game.engine.event.impl.StageEvent
import org.readutf.game.engine.event.listener.RegisteredListener
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Sits on top of the Minestom event system and allows for events
 * to be assigned to a given active game instance. Adapters for each event type
 * can be registered, including generic ones for abstract events like `PlayerEvent`.
 */
abstract class GameEventManager {
    private val logger = KotlinLogging.logger { }

    private val eventFilters: MutableMap<KClass<*>, EventGameAdapter> =
        mutableMapOf(
            StageEvent::class to StageEventGameAdapter(),
            GameEvent::class to GameEventGameAdapter(),
        )

    private val registeredTypes = mutableSetOf<KClass<*>>()
    private val noAdapters = mutableSetOf<KClass<*>>()
    private val registeredListeners = LinkedHashMap<GenericGame, LinkedHashMap<KClass<*>, MutableList<RegisteredListener>>>()
    private val eventStackTraceEnabled =
        mutableSetOf<KClass<*>>()

    fun <T : Any> callEvent(
        event: T,
        game: GenericGame,
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

        listeners.toList().forEach {
            try {
                it.gameListener.onEvent(event)
            } catch (e: Exception) {
                logger.error(e) { "Error occurred while calling ${event::class.simpleName} event listener" }
            }
        }
        return event
    }

    fun registerAdapter(clazz: KClass<*>, adapter: EventGameAdapter) {
        synchronized(this) {
            eventFilters
        }
    }

    private fun eventHandler(event: Any) {
        val eventType = event::class
        val adapters = findAdapter(event)

        if (adapters.isEmpty()) {
            if (!noAdapters.contains(eventType)) {
                noAdapters.add(eventType)
                logger.info { "No event adapter found for event type: $eventType" }
            }
            return
        }

        val foundGame =
            adapters
                .asSequence()
                .mapNotNull { it.convert(event) }
                .firstOrNull()

        if (foundGame == null) return

        try {
            callEvent(event, foundGame)
        } catch (e: Exception) {
            logger.error(e) { "Error occurred while calling ${event::class.simpleName} event listener" }
        }
    }

    fun registerListener(
        game: GenericGame,
        type: KClass<*>,
        registeredListener: RegisteredListener,
    ) {
        if (!registeredTypes.contains(type)) {
            registeredTypes.add(type)
            println("Registering listener for event type: $type")
            registerListener(type) { event ->
                eventHandler(event)
            }
        }

        val listeners =
            registeredListeners
                .getOrPut(game) { LinkedHashMap() } // Game Listeners
                .getOrPut(type) { mutableListOf() } // EventType listeners

        listeners.add(registeredListener)
        listeners.sortBy { it.priority }
    }

    abstract fun registerListener(type: KClass<*>, listener: (Any) -> Unit)

    fun unregisterEvent(
        game: GenericGame,
        kClass: KClass<*>,
        registeredListener: RegisteredListener,
    ) {
        val listeners = registeredListeners[game]?.get(kClass) ?: return
        if (listeners.remove(registeredListener)) {
            logger.info { "Unregistered listener for event type: $kClass" }
        }
    }

    private fun findAdapter(event: Any): Collection<EventGameAdapter> {
        synchronized(this) {
            return eventFilters.filterKeys { kClass -> event::class.isSubclassOf(kClass) }.values
        }
    }
}
