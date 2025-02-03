package org.readutf.game.engine.features.scoreboard

import java.util.UUID

abstract class ScoreboardManager<T> {

    abstract fun setScoreboard(
        playerId: UUID,
        scoreboard: Scoreboard<T>,
    )

    abstract fun removeScoreboard(playerId: UUID)
}
