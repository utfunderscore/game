package org.readutf.game.engine.debug.scoreboard

import org.readutf.neolobby.scoreboard.Scoreboard
import java.util.UUID

interface ScoreboardPlatform {

    fun setScoreboard(player: UUID, scoreboard: Scoreboard)

    fun removeScoreboard(player: UUID)

    fun updateScoreboard(player: UUID)
}
