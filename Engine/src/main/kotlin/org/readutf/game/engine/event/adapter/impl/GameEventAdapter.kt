package org.readutf.game.engine.event.adapter.impl

import net.minestom.server.event.Event
import org.readutf.game.engine.Game
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.adapter.EventAdapter

class GameEventAdapter : EventAdapter {
    override fun convert(event: Event): Game<*>? {
        if (event is GameEvent) return event.game
        return null
    }
}
