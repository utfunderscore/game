package org.readutf.game.server.game.dual.impl

import org.readutf.game.engine.Game
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.event.impl.GameRespawnEvent
import org.readutf.game.engine.respawning.RespawnPosition
import org.readutf.game.engine.stage.Stage
import org.readutf.game.server.game.dual.impl.cage.CageCreator
import org.readutf.game.server.game.dual.stages.FightingStage
import org.readutf.game.server.game.dual.utils.DualArena

class TheBridgeStage(
    val localGame: Game<out DualArena>,
    previousStage: Stage?,
    val cageCreator: CageCreator,
) : FightingStage(localGame, previousStage) {
    @EventListener
    fun onRespawn(e: GameRespawnEvent) {
        val location = e.respawnPositionResult.position.add(0.0, 0.5, 0.0)

        e.respawnPositionResult =
            RespawnPosition(
                location,
                e.player.instance,
                true,
            )

        val batch = cageCreator.createCage(e.player, location, localGame)

        batch.apply(e.player.instance) {
        }
    }
}
