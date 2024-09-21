package org.readutf.game.engine.event

import net.minestom.server.event.Event
import org.readutf.game.engine.Game

open class GameEvent(
    val game: Game<*>,
) : Event {
    var cancelled = false
}
