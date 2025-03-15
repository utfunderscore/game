package org.readutf.game.engine.event.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent
import java.util.UUID

class GameLeaveEvent(
    game: GenericGame,
    val player: UUID,
) : GameEvent(game)
