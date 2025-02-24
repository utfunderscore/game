package org.readutf.game.minestom.platform.adapters

import net.minestom.server.event.trait.InstanceEvent
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventGameAdapter

class InstanceEventGameAdapter : EventGameAdapter {
    override fun convert(event: Any): GenericGame? {
        if (event !is InstanceEvent) return null

        return GameManager.activeGames.values.firstOrNull {
            it.arena?.instance == event.instance
        }
    }
}
