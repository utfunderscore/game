package org.readutf.game.server.game

import org.readutf.game.engine.Game

interface GameCreator<T : Game<*>> {
    fun create(game: T)
}
