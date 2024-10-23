package org.readutf.game.engine.event

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.entity.EntitySpawnEvent
import net.minestom.server.event.trait.EntityEvent
import net.minestom.server.event.trait.InstanceEvent
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventGameAdapter
import org.readutf.game.engine.event.adapter.game.EntityEventGameAdapter
import org.readutf.game.engine.event.adapter.game.GameEventGameAdapter
import org.readutf.game.engine.event.adapter.game.InstanceEventGameAdapter
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
object GameEventManager {
    private val logger = KotlinLogging.logger { }

    private val eventFilters: Map<KClass<out Event>, EventGameAdapter> =
        mapOf(
            StageEvent::class to StageEventGameAdapter(),
            EntityEvent::class to EntityEventGameAdapter(),
            GameEvent::class to GameEventGameAdapter(),
            InstanceEvent::class to InstanceEventGameAdapter(),
        )

    private val registeredTypes = mutableSetOf<KClass<out Event>>()
    private val noAdapters = mutableSetOf<KClass<out Event>>()
    private val registeredListeners = LinkedHashMap<GenericGame, LinkedHashMap<KClass<out Event>, MutableList<RegisteredListener>>>()
    private val eventStackTraceEnabled =
        mutableSetOf<KClass<out Event>>(
            EntitySpawnEvent::class,
        )


    init {
        eventStackTraceEnabled.add(EntityDamageEvent::class)
    }

    fun <T : Event> callEvent(
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

    private fun eventHandler(event: Event) {
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
        kClass: KClass<out Event>,
        registeredListener: RegisteredListener,
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

        listeners.add(registeredListener)
        listeners.sortBy { it.priority }
    }

    fun unregisterEvent(
        game: GenericGame,
        kClass: KClass<out Event>,
        registeredListener: RegisteredListener,
    ) {
        val listeners = registeredListeners[game]?.get(kClass) ?: return
        if (listeners.remove(registeredListener)) {
            logger.info { "Unregistered listener for event type: $kClass" }
        }
    }

    private fun findAdapter(event: Event): Collection<EventGameAdapter> {
        synchronized(this) {
            return eventFilters.filterKeys { kClass -> event::class.isSubclassOf(kClass) }.values
        }
    }
}
