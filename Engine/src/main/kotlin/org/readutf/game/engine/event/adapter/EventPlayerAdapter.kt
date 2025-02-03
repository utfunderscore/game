package org.readutf.game.engine.event.adapter

import net.minestom.server.entity.Player
import net.minestom.server.event.Event

/**
 * Defines how a game can be determined from a given event
 */
interface EventPlayerAdapter {
    fun convert(event: Event): Player?
}
