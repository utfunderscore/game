package org.readutf.game.engine.event.adapter

import org.readutf.game.engine.platform.player.GamePlayer

/**
 * Defines how a game can be determined from a given event
 */
interface EventPlayerAdapter {
    fun convert(event: Any): GamePlayer?
}
