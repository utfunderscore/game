package org.readutf.game.engine.event.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.Cancellable
import org.readutf.game.engine.event.GameEvent

class GameTeamAddEvent(
    game: GenericGame,
) : GameEvent(game),
    Cancellable {
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}
