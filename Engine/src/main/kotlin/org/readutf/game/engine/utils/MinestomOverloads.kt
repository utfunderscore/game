package org.readutf.game.engine.utils

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import java.util.function.Consumer

inline fun <reified T : Event> EventNode<Event>.addListener(noinline listener: (T) -> Unit): EventNode<Event> =
    addListener(T::class.java) {
        listener(it)
    }

inline fun <reified T : Event> EventNode<Event>.addListener(listener: Consumer<T>) {
    addListener(T::class.java, listener)
}

private val legacySerializer = LegacyComponentSerializer.legacy('&')

fun String.toComponent() = legacySerializer.deserialize(this)
