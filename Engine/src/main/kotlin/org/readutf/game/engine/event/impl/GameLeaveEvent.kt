package org.readutf.game.engine.event.impl

import net.minestom.server.entity.Player
import org.readutf.game.engine.Game
import org.readutf.game.engine.event.GameEvent

class GameLeaveEvent(
    game: Game<*>,
    val player: Player,
) : GameEvent(game)
