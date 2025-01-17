package org.readutf.game.engine.event.adapter.game

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.event.adapter.EventGameAdapter

class GameEventGameAdapter : EventGameAdapter {
    override fun convert(event: Any): GenericGame? {
        if (event is GameEvent) return event.game
        return null
    }
}
