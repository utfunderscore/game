package org.readutf.game.engine.features

import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.event.listener.TypedGameListener
import kotlin.reflect.KClass

abstract class Feature {

    val listeners = mutableMapOf<GameListener, KClass<*>>()

    fun registerListener(gameListener: GameListener, kClass: KClass<*>) {
        listeners[gameListener] = kClass
    }

    inline fun <reified T : Any> registerListener(typedGameListener: TypedGameListener<T>) {
        registerListener(typedGameListener, T::class)
    }
}
