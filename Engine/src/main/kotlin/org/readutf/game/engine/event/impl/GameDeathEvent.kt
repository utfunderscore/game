package org.readutf.game.engine.event.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import org.readutf.game.engine.platform.player.GamePlayer

class GameDeathEvent(
    game: GenericGame,
    val player: GamePlayer,
    val damageType: String,
) : GameEvent(game)
