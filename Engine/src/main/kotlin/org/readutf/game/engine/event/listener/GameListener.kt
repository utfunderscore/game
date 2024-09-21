package org.readutf.game.engine.event.listener

import net.minestom.server.event.Event

interface GameListener {
    fun onEvent(event: Event)
}
