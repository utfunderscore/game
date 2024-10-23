package org.readutf.game.server.game.dual.stages

import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.team.GameTeam

class VictoryStage<ARENA : Arena<*>, TEAM : GameTeam>(
    game: Game<ARENA, TEAM>,
    previousStage: Stage<ARENA, TEAM>?,
    winner: GameTeam?,
) : Stage<ARENA, TEAM>(game, previousStage)
