package org.readutf.game.engine.debug.scoreboard

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.schedular.RepeatingGameTask
import org.readutf.neolobby.scoreboard.Scoreboard
import java.util.UUID

class ScoreboardManager(game: GenericGame, private val scoreboardPlatform: ScoreboardPlatform) {
    private val scoreboards = mutableMapOf<UUID, Scoreboard>()

    init {
        game.scheduler.schedule(ScoreboardTask())
    }

    fun setScoreboard(
        player: GamePlayer,
        board: Scoreboard,
    ) {
        scoreboards[player.uuid] = board
        scoreboardPlatform.setScoreboard(player.uuid, board)
    }

    inner class ScoreboardTask : RepeatingGameTask(0, 50L) {
        override fun tick() {
            scoreboards.forEach { (uuid, board) ->
                scoreboardPlatform.updateScoreboard(uuid)
            }
        }
    }
}
