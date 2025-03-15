package org.readutf.game.engine.event.listener

fun interface TypedGameListener<T : Any> : GameListener {
    fun onTypedEvent(event: T)

    @Suppress("UNCHECKED_CAST")
    override fun onEvent(event: Any) {
        onTypedEvent(event as T)
    }
}
