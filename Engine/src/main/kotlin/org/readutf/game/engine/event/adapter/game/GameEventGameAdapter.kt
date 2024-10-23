package org.readutf.game.engine.event.adapter.game

import net.minestom.server.event.Event
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.adapter.EventGameAdapter

class GameEventGameAdapter : EventGameAdapter {
    override fun convert(event: Event): GenericGame? {
        if (event is GameEvent) return event.game
        return null
    }
}
