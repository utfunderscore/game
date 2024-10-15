package org.readutf.game.engine.stage

import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.types.Result

fun interface StageCreator<ARENA : Arena<*>, TEAM : GameTeam> {
    fun create(
        game: Game<ARENA, TEAM>,
        previousStage: Stage?,
    ): Result<Stage>
}
