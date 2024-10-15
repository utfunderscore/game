package org.readutf.game.server.game

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.types.Result

fun interface GameCreator<T : GenericGame> {
    fun create(): Result<T>
}
