package org.readutf.game.engine.features.scoreboard

import org.readutf.game.engine.features.Feature
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.engine.schedular.RepeatingGameTask
import java.util.UUID

abstract class ScoreboardFeature<T> : Feature() {

    val scoreboards = mutableMapOf<UUID, org.readutf.game.engine.features.scoreboard.Scoreboard<T>>()

    fun setScoreboard(
        playerId: UUID,
        scoreboard: org.readutf.game.engine.features.scoreboard.Scoreboard<T>,
    ) {
        scoreboards[playerId] = scoreboard
        refreshScoreboard(playerId, scoreboard)
    }

    override fun getTasks(): List<GameTask> = listOf(Task())

    abstract fun refreshScoreboard(
        playerId: UUID,
        scoreboard: org.readutf.game.engine.features.scoreboard.Scoreboard<T>,
    )

    private inner class Task : RepeatingGameTask(delay = 0, period = 1) {
        override fun run() {
            scoreboards.forEach { (playerId, scoreboard) ->
                refreshScoreboard(playerId, scoreboard)
            }
        }
    }
}
