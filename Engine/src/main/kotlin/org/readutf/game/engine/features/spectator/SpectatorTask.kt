package org.readutf.game.engine.features.spectator

import net.minestom.server.entity.Player
import org.readutf.game.engine.Game
import org.readutf.game.engine.schedular.CountdownTask

class SpectatorTask(
    game: Game<*>,
    val player: Player,
    duration: Int,
    val spectatorManager: SpectatorManager,
) : CountdownTask(game, duration, spectatorManager.intervals) {
    override fun onInterval(interval: Int) {
        if (interval <= 0) {
            spectatorManager.removeSpectator(player)
        }

        spectatorManager.countdownHandler.onInterval(player, interval)
    }
}
