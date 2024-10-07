package org.readutf.game.engine.event.impl

import net.minestom.server.entity.Player
import org.readutf.game.engine.Game
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.respawning.RespawnPosition

class GameRespawnEvent(
    game: Game<*>,
    val player: Player,
    var respawnPositionResult: RespawnPosition,
) : GameEvent(game)
