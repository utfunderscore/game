package org.readutf.game.engine.stage

import org.readutf.game.engine.Game

interface StageCreator {
    fun create(game: Game<*>): Stage
}
