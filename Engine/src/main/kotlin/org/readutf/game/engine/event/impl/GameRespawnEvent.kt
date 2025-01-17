package org.readutf.game.engine.event.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.respawning.RespawnPosition

class GameRespawnEvent(
    game: GenericGame,
    val player: GamePlayer,
    var respawnPositionResult: RespawnPosition,
) : GameEvent(game)
