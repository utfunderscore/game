package org.readutf.game.engine.stage

import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.types.Result

fun interface StageCreator<ARENA : Arena<*>> {
    fun create(game: Game<ARENA>): Result<Stage>
}
