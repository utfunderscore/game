package org.readutf.game.server.game.dual.stage.awaitingplayers

import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.event.impl.GameJoinEvent
import org.readutf.game.engine.respawning.impl.TeamIdSpawnHandler
import org.readutf.game.engine.schedular.RepeatingGameTask
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.utils.toComponent
import org.readutf.game.server.game.dual.DualGame
import java.util.concurrent.TimeUnit

class AwaitingPlayersStage(
    override val game: DualGame,
) : Stage(game) {
    init {
        game.respawnHandler = TeamIdSpawnHandler(game, "spawn")
        game.scheduler.schedule(this, CountdownTask(listOf(60, 30, 15, 10, 5, 4, 3, 2, 1)))
    }

    @EventListener
    fun onGameJoin(gameJoinEvent: GameJoinEvent) {
        if (game.getOnlinePlayers().count() >= game.dualGameSettings.maxPlayers) {
            gameJoinEvent.setCancelled(true)
        }
    }

    inner class CountdownTask(
        intervalAlerts: List<Int>,
    ) : RepeatingGameTask(1000, 50) {
        private var countdownStart: Long? = null
        private var lastAlert = 0L

        private val unsentAlerts: MutableList<Int> =
            intervalAlerts
                .filter { it < game.dualGameSettings.minPlayersCountdown }
                .toMutableList()

        override fun tick() {
            if (game.dualGameSettings.minPlayersCountdown <= 0) {
                endStage().onFailure(game::crash)
                return
            }

            if (System.currentTimeMillis() - lastAlert > TimeUnit.SECONDS.toMillis(15)) {
                game.messageAll(
                    "Waiting for ${game.dualGameSettings.minStartPlayers - game.getPlayers().count()} more players...".toComponent(),
                )
                lastAlert = System.currentTimeMillis()
            }

            if (game.getOnlinePlayers().count() >= game.dualGameSettings.minStartPlayers) {
                if (countdownStart == null) {
                    game.messageAll("Starting in 10 seconds".toComponent())
                    countdownStart = System.currentTimeMillis()
                } else {
                    val timePassed = System.currentTimeMillis() - countdownStart!!
                    val timeLeft = game.dualGameSettings.minPlayersCountdown * 1000 - timePassed
                    val secondsLeft: Int = (timeLeft / 1000).toInt()

                    if (timeLeft <= 0) {
                        endStage().onFailure(game::crash)
                    } else if (unsentAlerts.contains(secondsLeft)) {
                        unsentAlerts.remove(secondsLeft)
                        game.messageAll("Starting in $secondsLeft seconds".toComponent())
                    }
                }
            } else if (countdownStart != null) {
                countdownStart = null

                game.messageAll(
                    "Waiting for ${game.dualGameSettings.minStartPlayers - game.getPlayers().count()} more players...".toComponent(),
                )
            }
        }
    }
}
