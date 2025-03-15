package org.readutf.game.minestom.platform.adapters

import net.minestom.server.entity.Player
import net.minestom.server.event.trait.EntityEvent
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventGameAdapter

class EntityEventGameAdapter : EventGameAdapter {
    override fun convert(event: Any): GenericGame? {
        if (event !is EntityEvent) return null

        val entity = event.entity
        if (entity is Player) {
            GameManager.playerToGame[entity.uuid]?.let {
                return it
            }
        }
        val instance = entity.instance

        return GameManager.activeGames.values.firstOrNull {
            it.arena?.instance == instance
        }
    }
}
