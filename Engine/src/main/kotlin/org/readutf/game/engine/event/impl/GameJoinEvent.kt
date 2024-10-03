package org.readutf.game.engine.event.impl

import net.minestom.server.entity.Player
import org.readutf.game.engine.Game
import org.readutf.game.engine.event.Cancellable
import org.readutf.game.engine.event.GameEvent

class GameJoinEvent(
    game: Game<*>,
    val player: Player,
) : GameEvent(game),
    Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}
