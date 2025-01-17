package org.readutf.game.engine.utils

class EventListener<T : Any>(
    val listener: (T) -> Unit,
)

// inline fun <reified T : Event> org.readutf.game.engine.utils.EventListener<T>.toListener(): MinestomEventListener<T> =
//    object : MinestomEventListener<T> {
//        override fun eventType(): Class<T> = T::class.java
//
//        override fun run(event: T): EventListener.Result = listener(event)
//    }
