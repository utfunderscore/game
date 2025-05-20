package org.readutf.game.minestom.platform.feature

import net.minestom.server.coordinate.Pos
import org.readutf.buildformat.common.markers.Position
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.features.respawning.RespawnHandler
import org.readutf.game.engine.game.features.respawning.RespawningFeature
import org.readutf.game.engine.world.GameWorld
import org.readutf.game.minestom.platform.MinestomWorld
import org.readutf.game.minestom.utils.toPlayer
import org.readutf.game.minestom.utils.toPoint
import java.util.UUID

class MinestomRespawningFeature(
    game: GenericGame,
    respawnHandler: RespawnHandler,
) : RespawningFeature(game, respawnHandler) {
    override fun teleport(
        playerId: UUID,
        gameWorld: GameWorld,
        position: Position,
    ) {
        val player = playerId.toPlayer() ?: return
        val instance = (gameWorld as MinestomWorld).instance

        if (player.instance == instance) {
            player.teleport(Pos(position.toPoint()))
        } else {
            player.setInstance(instance, Pos(position.toPoint()))
        }
    }

    override fun shutdown() {
    }
}
