package org.readutf.game.server.game.dual.stages

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.team.GameTeam

class VictoryStage(
    game: GenericGame,
    previousStage: Stage?,
    winner: GameTeam,
) : Stage(game, previousStage)
