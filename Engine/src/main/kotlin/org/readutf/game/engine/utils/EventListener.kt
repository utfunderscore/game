package org.readutf.game.engine.utils

import net.minestom.server.event.Event
import net.minestom.server.event.EventListener
import net.minestom.server.event.EventListener as MinestomEventListener

class EventListener<T : Event>(
    val listener: (T) -> MinestomEventListener.Result,
)

inline fun <reified T : Event> org.readutf.game.engine.utils.EventListener<T>.toListener(): MinestomEventListener<T> =
    object : MinestomEventListener<T> {
        override fun eventType(): Class<T> = T::class.java

        override fun run(event: T): EventListener.Result = listener(event)
    }
