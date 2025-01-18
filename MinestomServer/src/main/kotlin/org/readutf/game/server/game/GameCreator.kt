package org.readutf.game.server.game

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.utils.SResult

fun interface GameCreator<T : GenericGame> {
    fun create(): SResult<T>
}
