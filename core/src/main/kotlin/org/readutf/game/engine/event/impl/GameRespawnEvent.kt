package org.readutf.game.engine.event.impl

import org.readutf.buildformat.common.markers.Position
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.Cancellable
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.world.GameWorld
import java.util.*

class GameRespawnEvent(game: GenericGame, val playerId: UUID, var world: GameWorld, var respawnLocation: Position) :
    GameEvent(game),
    Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}
