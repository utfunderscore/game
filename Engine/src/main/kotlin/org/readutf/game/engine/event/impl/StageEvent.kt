package org.readutf.game.engine.event.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.stage.GenericStage

class StageEvent(
    val stage: GenericStage,
    game: GenericGame,
) : GameEvent(game)
