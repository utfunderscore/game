package org.readutf.game.engine.event.listener

data class RegisteredListener(
    var gameListener: GameListener,
    var ignoreCancelled: Boolean,
    var ignoreSpectators: Boolean,
    var priority: Int,
)
