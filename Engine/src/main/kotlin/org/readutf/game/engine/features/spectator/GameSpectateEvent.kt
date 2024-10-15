package org.readutf.game.engine.features.spectator

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.Cancellable
import org.readutf.game.engine.event.GameEvent

class GameSpectateEvent(
    game: GenericGame,
    val player: Player,
    val respawnLocation: Pos,
    var respawnTime: Int,
    var respawn: Boolean,
) : GameEvent(game),
    Cancellable {
    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }
}
