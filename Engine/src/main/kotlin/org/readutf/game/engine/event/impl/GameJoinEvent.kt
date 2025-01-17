package org.readutf.game.engine.event.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.Cancellable
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.platform.player.GamePlayer

class GameJoinEvent(
    game: GenericGame,
    val player: GamePlayer,
) : GameEvent(game),
    Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}
