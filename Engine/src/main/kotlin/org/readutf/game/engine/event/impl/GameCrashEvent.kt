package org.readutf.game.engine.event.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEvent

/**
 * Called when a game crashes or is forced to stop.
 */
class GameCrashEvent(
    game: GenericGame,
) : GameEvent(game)
