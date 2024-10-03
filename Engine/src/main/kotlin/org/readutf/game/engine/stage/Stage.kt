package org.readutf.game.engine.stage

import net.minestom.server.event.Event
import org.readutf.game.engine.Game
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.types.Result
import kotlin.reflect.KClass

open class Stage(
    open val game: Game<*>,
) {
    val registeredListeners = LinkedHashMap<KClass<out Event>, MutableList<GameListener>>()

    open fun onStart(previousStage: Stage?): Result<Unit> = Result.empty()

    open fun onFinish(): Result<Unit> = Result.empty()

    fun registerListener(
        gameListener: GameListener,
        type: KClass<out Event>,
    ) {
        registeredListeners
            .getOrPut(type) { mutableListOf() }
            .add(gameListener)

        GameEventManager.registerEvent(game, type, gameListener)
    }

    inline fun <reified T : Event> registerListener(gameListener: TypedGameListener<T>) {
        registeredListeners
            .getOrPut(T::class) { mutableListOf() }
            .add(gameListener)

        GameEventManager.registerEvent(game, T::class, gameListener)
    }

    fun endStage() = game.startNextStage()

    internal fun unregisterListeners() {
        registeredListeners.forEach { (type, listeners) ->
            listeners.forEach { listener ->
                GameEventManager.unregisterEvent(game, type, listener)
            }
        }
    }
}
