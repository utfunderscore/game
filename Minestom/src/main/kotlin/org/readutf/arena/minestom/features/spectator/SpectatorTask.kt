package org.readutf.arena.minestom.features.spectator

import net.minestom.server.entity.Player
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.schedular.CountdownTask

class SpectatorTask(
    game: GenericGame,
    val player: Player,
    duration: Int,
    val spectatorManager: SpectatorManager,
) : CountdownTask(game, duration, spectatorManager.intervals.filter { it <= duration }) {
    override fun onInterval(interval: Int) {
        if (interval <= 0) {
            spectatorManager.removeSpectator(player)
            return
        }

        spectatorManager.countdownHandler.onInterval(player, interval)
    }
}
