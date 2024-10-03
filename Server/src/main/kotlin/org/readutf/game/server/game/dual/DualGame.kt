package org.readutf.game.server.game.dual

import org.readutf.game.engine.Game
import org.readutf.game.engine.stage.StageCreator
import org.readutf.game.engine.types.toSuccess
import org.readutf.game.server.game.dual.stage.awaitingplayers.AwaitingPlayersStage
import org.readutf.game.server.game.dual.stage.awaitingplayers.FightingStage
import org.readutf.game.server.game.dual.utils.DualArena

class DualGame(
    dualArena: DualArena,
    val dualGameSettings: DualGameSettings,
) : Game<DualArena>() {
    init {
        changeArena(dualArena)

        registerStage(
            StageCreator { AwaitingPlayersStage(it as DualGame).toSuccess() },
            StageCreator { FightingStage(it as DualGame).toSuccess() },
        )
    }
}
