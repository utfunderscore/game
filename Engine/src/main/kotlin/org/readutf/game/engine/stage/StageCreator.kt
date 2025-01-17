package org.readutf.game.engine.stage

import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.utils.SResult

fun interface StageCreator<WORLD : ArenaWorld, ARENA : Arena<*, WORLD>, TEAM : GameTeam> {
    fun create(
        game: Game<WORLD, ARENA, TEAM>,
        previousStage: Stage<WORLD, ARENA, TEAM>?,
    ): SResult<Stage<WORLD, ARENA, TEAM>>
}
