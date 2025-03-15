package org.readutf.game.engine.stage

import com.github.michaelbull.result.Result
import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.team.GameTeam

fun interface StageCreator<ARENA : Arena<*>, TEAM : GameTeam> {
    fun create(
        game: Game<ARENA, TEAM>,
        previousStage: Stage<ARENA, TEAM>?,
    ): Result<Stage<ARENA, TEAM>, Throwable>
}
