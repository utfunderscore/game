package org.readutf.game.engine.event.adapter.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventGameAdapter
import org.readutf.game.engine.event.impl.StageEvent

class StageEventGameAdapter : EventGameAdapter {
    override fun convert(event: Any): GenericGame? {
        if (event is StageEvent && event.game.currentStage == event.stage) {
            return event.game
        }
        return null
    }
}
