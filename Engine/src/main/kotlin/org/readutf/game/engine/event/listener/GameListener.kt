package org.readutf.game.engine.event.listener

import net.minestom.server.event.Event

fun interface GameListener {
    fun onEvent(event: Event)
}
