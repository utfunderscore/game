package org.readutf.game.engine.features

import org.readutf.game.engine.event.listener.GameListener

abstract class Feature {

    private val listeners = mutableListOf<GameListener>()

    fun registerListener(gameListener: GameListener) {
        listeners.add(gameListener)
    }
}
