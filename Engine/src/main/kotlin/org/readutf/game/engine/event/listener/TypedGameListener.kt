package org.readutf.game.engine.event.listener

import net.minestom.server.event.Event

fun interface TypedGameListener<T : Event> : GameListener {
    fun onTypedEvent(event: T)

    @Suppress("UNCHECKED_CAST")
    override fun onEvent(event: Event) {
        onTypedEvent(event as T)
    }
}
