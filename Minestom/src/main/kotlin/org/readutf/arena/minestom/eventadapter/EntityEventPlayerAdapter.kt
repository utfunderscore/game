package org.readutf.arena.minestom.eventadapter

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.EntityEvent
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventGameAdapter

class EntityEventPlayerAdapter : EventGameAdapter {
    override fun convert(event: Any): GenericGame? {
        if (event !is EntityEvent) return null

        val entity = event.entity
        if (entity is Player) {
            return GameManager.playerToGame[entity.uuid]
        }
        return null
    }
}
