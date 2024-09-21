package org.readutf.game.engine.utils

import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena

class GameBuilder<T : Arena<*>>(
    val context: Game<T>.() -> Unit,
) {
    private val game = Game<T>()

    fun build(): Game<T> {
        context(game)
        return game
    }
}
