package org.readutf.game.server.game.impl

import org.readutf.game.engine.Game
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.types.toSuccess
import org.readutf.game.server.game.dual.stages.AwaitingPlayersStage
import org.readutf.game.server.game.dual.utils.DualArena

class TheBridgeGame(
    arena: DualArena,
    settings: TheBridgeSettings,
    kitManager: KitManager,
) : Game<DualArena>() {
    val kit = kitManager.loadKit("thebridge").getOrThrow()

    init {

        changeArena(arena)

        registerTeam("red")
        registerTeam("blue")

        registerStage(
            AwaitingPlayersStage.Creator(settings.awaitingPlayersSettings),
            { game, previousStage -> TheBridgeStage(game, previousStage).toSuccess() },
        )
    }
}
