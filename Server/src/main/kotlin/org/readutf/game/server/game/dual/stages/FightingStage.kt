package org.readutf.game.server.game.dual.stages

import org.readutf.game.engine.Game
import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.types.Result

open class FightingStage(
    game: Game<*>,
    previousStage: Stage?,
) : Stage(game, previousStage) {
    override fun onStart(): Result<Unit> {
        game.getOnlinePlayers().forEach {
            game.spawnPlayer(it).mapError { error -> return error }
        }

        return Result.empty()
    }
}
