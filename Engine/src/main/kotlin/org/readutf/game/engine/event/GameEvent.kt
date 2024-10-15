package org.readutf.game.engine.event

import net.minestom.server.event.Event
import org.readutf.game.engine.GenericGame

open class GameEvent(
    val game: GenericGame,
) : Event
