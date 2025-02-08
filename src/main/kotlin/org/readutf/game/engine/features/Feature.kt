package org.readutf.game.engine.features

import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.engine.schedular.RepeatingGameTask
import kotlin.reflect.KClass

abstract class Feature {
    open fun getListeners(): Map<KClass<*>, GameListener> = emptyMap()

    open fun getTasks(): List<GameTask> = emptyList()
}
