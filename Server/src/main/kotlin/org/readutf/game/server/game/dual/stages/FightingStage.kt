package org.readutf.game.server.game.dual.stages

import org.readutf.game.engine.stage.Stage
import org.readutf.game.engine.types.Result

class FightingStage(
    game: DualGame,
) : Stage(game) {
    override fun onStart(previousStage: Stage?): Result<Unit> {
        game.getOnlinePlayers().forEach {
            game.spawnPlayer(it).mapError { error -> return error }
        }

        return Result.empty()
    }
}
