package org.readutf.game.engine.stage

import net.minestom.server.event.Event
import org.readutf.game.engine.GenericGame
import org.readutf.game.engine.event.GameEventManager
import org.readutf.game.engine.event.annotation.scan
import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.engine.types.Result
import kotlin.reflect.KClass

abstract class Stage(
    open val game: GenericGame,
    val previousStage: Stage?,
) {
    val registeredListeners = LinkedHashMap<KClass<out Event>, MutableList<GameListener>>()

    open fun onStart(): Result<Unit> = Result.empty()

    open fun onFinish(): Result<Unit> = Result.empty()

    fun registerListener(
        gameListener: GameListener,
        type: KClass<out Event>,
    ) {
        registeredListeners
            .getOrPut(type) { mutableListOf() }
            .add(gameListener)

        GameEventManager.registerListener(game, type, gameListener)
    }

    inline fun <reified T : Event> registerListener(gameListener: TypedGameListener<T>) {
        registeredListeners
            .getOrPut(T::class) { mutableListOf() }
            .add(gameListener)

        GameEventManager.registerListener(game, T::class, gameListener)
    }

    fun registerAll(any: Any) {
        val scan = scan(any).getOrThrow()

        scan.forEach { (type, listener) ->
            registerListener(listener, type as KClass<out Event>)
        }
    }

    fun schedule(gameTask: GameTask) {
        game.scheduler.schedule(this, gameTask)
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
