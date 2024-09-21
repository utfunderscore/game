package org.readutf.game.engine.event.adapter

import net.minestom.server.event.Event
import org.readutf.game.engine.Game

/**
 * Defines how a game can be determined from a given event
 */
interface EventAdapter {
    fun convert(event: Event): Game<*>?
}
