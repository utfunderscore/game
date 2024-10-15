package org.readutf.game.engine.utils

import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.team.GameTeam

class GameBuilder<ARENA : Arena<*>, TEAM : GameTeam>(
    val context: Game<ARENA, TEAM>.() -> Unit,
) {
    private val game = Game<ARENA, TEAM>()

    fun build(): Game<ARENA, TEAM> {
        context(game)
        return game
    }
}
