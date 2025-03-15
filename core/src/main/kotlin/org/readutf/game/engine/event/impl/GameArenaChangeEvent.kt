package org.readutf.game.engine.event.impl

import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.GameEvent

class GameArenaChangeEvent(game: GenericGame, val arena: Arena<*>, previousArena: Arena<*>?) : GameEvent(game)
