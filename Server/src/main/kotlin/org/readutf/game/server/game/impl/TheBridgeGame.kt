package org.readutf.game.server.game.impl

import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.kit.KitManager
import org.readutf.game.engine.types.toSuccess
import org.readutf.game.server.game.dual.stages.AwaitingPlayersStage

class TheBridgeGame(
    arena: Arena<TheBridgePositions>,
    settings: TheBridgeSettings,
    kitManager: KitManager,
) : Game<Arena<TheBridgePositions>, TheBridgeTeam>() {
    val kit = kitManager.loadKit("thebridge").getOrThrow()

    init {

        changeArena(arena)

        registerTeam("red")
        registerTeam("blue")

        registerStage(
            AwaitingPlayersStage.Creator(settings.awaitingPlayersSettings, arena.positionSettings.dualGamePositions),
            { _, previousStage -> TheBridgeStage(this, previousStage).toSuccess() },
        )
    }
}
