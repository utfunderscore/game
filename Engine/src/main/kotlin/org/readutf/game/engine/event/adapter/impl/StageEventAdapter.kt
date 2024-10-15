package org.readutf.game.engine.event.adapter.impl

import net.minestom.server.event.Event
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventAdapter
import org.readutf.game.engine.event.impl.StageEvent

class StageEventAdapter : EventAdapter {
    override fun convert(event: Event): GenericGame? {
        if (event is StageEvent && event.game.currentStage == event.stage) {
            return event.game
        }
        return null
    }
}
