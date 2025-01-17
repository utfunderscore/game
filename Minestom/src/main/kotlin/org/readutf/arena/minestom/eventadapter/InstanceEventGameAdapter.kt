package org.readutf.arena.minestom.eventadapter

import net.minestom.server.event.trait.InstanceEvent
import org.readutf.arena.minestom.platform.MinestomWorld
import org.readutf.game.engine.GameManager
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.adapter.EventGameAdapter

class InstanceEventGameAdapter : EventGameAdapter {
    override fun convert(event: Any): GenericGame? {
        if (event !is InstanceEvent) return null

        return GameManager.activeGames.values.firstOrNull {
            it.arena?.arenaWorld is MinestomWorld && (it.arena?.arenaWorld as MinestomWorld).instance == event.instance
        }
    }
}
