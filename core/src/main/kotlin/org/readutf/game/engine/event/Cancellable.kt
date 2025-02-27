package org.readutf.game.engine.event

interface Cancellable {
    fun isCancelled(): Boolean

    fun setCancelled(cancelled: Boolean)
}
