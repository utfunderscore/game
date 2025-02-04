package org.readutf.game.engine.event.adapter

import org.readutf.game.engine.GenericGame

/**
 * Defines how a game can be determined from a given event
 */
interface EventGameAdapter {
    fun convert(event: Any): GenericGame?
}
