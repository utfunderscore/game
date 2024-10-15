package org.readutf.game.engine.event.impl

import net.minestom.server.entity.Player
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.respawning.RespawnPosition

class GameRespawnEvent(
    game: GenericGame,
    val player: Player,
    var respawnPositionResult: RespawnPosition,
) : GameEvent(game)
