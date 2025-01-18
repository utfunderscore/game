package org.readutf.game.engine.feature.spectator

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.utils.Position

class GameSpectateEvent(
    game: GenericGame,
    val player: GamePlayer,
    var respawnLocation: Position,
    var respawnTime: Int,
    var respawn: Boolean,
) : GameEvent(game)
