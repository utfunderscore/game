package org.readutf.game.server.game

import org.readutf.game.engine.Game
import org.readutf.game.engine.types.Result

fun interface GameCreator<T : Game<*>> {
    fun create(): Result<T>
}
