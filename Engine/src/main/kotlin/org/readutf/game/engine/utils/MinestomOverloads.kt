package org.readutf.game.engine.utils

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import java.util.function.Consumer

inline fun <reified T : Event> EventNode<Event>.addListener(noinline listener: (T) -> Unit) {
    addListener(T::class.java) { listener(it) }
}

inline fun <reified T : Event> EventNode<Event>.addListener(listener: Consumer<T>) {
    addListener(T::class.java, listener)
}
