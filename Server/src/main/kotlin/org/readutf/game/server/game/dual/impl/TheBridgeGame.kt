package org.readutf.game.server.game.dual.impl

import org.readutf.game.engine.Game
import org.readutf.game.engine.types.toSuccess
import org.readutf.game.server.game.dual.impl.cage.CageCreator
import org.readutf.game.server.game.dual.stages.AwaitingPlayersStage
import org.readutf.game.server.game.dual.utils.DualArena

class TheBridgeGame(
    arena: DualArena,
    settings: TheBridgeSettings,
) : Game<DualArena>() {
    init {

        changeArena(arena)

        registerTeam("red")
        registerTeam("blue")

        registerStage(
            AwaitingPlayersStage.Creator(settings.awaitingPlayersSettings),
            { game, previousStage -> TheBridgeStage(game, previousStage, CageCreator.DefaultCageCreator()).toSuccess() },
        )
    }
}
