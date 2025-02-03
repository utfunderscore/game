package org.readutf.game.engine.event.adapter

import net.minestom.server.event.Event
import org.readutf.game.engine.GenericGame

/**
 * Defines how a game can be determined from a given event
 */
interface EventGameAdapter {
    fun convert(event: Event): GenericGame?
}
