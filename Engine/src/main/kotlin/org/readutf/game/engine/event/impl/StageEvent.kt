package org.readutf.game.engine.event.impl

import org.readutf.game.engine.Game
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.stage.Stage

class StageEvent(
    val stage: Stage,
    game: Game<*>,
) : GameEvent(game)
