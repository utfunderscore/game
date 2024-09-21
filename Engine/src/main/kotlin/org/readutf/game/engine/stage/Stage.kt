package org.readutf.game.engine.stage

import org.readutf.game.engine.Game

abstract class Stage(
    val game: Game<*>,
) {
    abstract fun onStart(previousStage: Stage)

    abstract fun onFinish(previousStage: Stage)
}
