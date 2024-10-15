package org.readutf.game.engine.event.adapter.impl

import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.trait.EntityEvent
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventAdapter

class EntityEventAdapter : EventAdapter {
    override fun convert(event: Event): GenericGame? {
        if (event !is EntityEvent) return null

        val entity = event.entity
        if (entity is Player) {
            return GameManager.playerToGame[entity.uuid]
        }
        return null
    }
}
