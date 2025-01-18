package org.readutf.game.engine.feature.spectator

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.schedular.CountdownTask

class SpectatorTask(
    game: GenericGame,
    val player: GamePlayer,
    duration: Int,
    private val spectatorManager: SpectatorManager,
) : CountdownTask(game, duration, spectatorManager.intervals.filter { it <= duration }) {
    override fun onInterval(interval: Int) {
        if (interval <= 0) {
            spectatorManager.removeSpectator(player)
            return
        }

        spectatorManager.countdownHandler.onInterval(player, interval)
    }
}
