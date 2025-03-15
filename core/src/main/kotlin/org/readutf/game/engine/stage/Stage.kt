package org.readutf.game.engine.stage

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrThrow
import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.annotation.scan
import org.readutf.game.engine.event.listener.RegisteredListener
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.features.Feature
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.engine.team.GameTeam
import kotlin.reflect.KClass

typealias GenericStage = Stage<*, *>

abstract class Stage<ARENA : Arena<*>, TEAM : GameTeam>(
    open val game: Game<ARENA, TEAM>,
    val previousStage: Stage<ARENA, TEAM>?,
) {
    private val startTime = System.currentTimeMillis()
    val features = mutableListOf<Feature>()
    private val registeredListeners = LinkedHashMap<KClass<*>, MutableList<RegisteredListener>>()

    open fun onStart(): Result<Unit, Throwable> = Ok(Unit)

    open fun onFinish(): Result<Unit, Throwable> = Ok(Unit)

    fun <T : Feature> addFeature(feature: T): T {
        for ((type, listener) in feature.getListeners()) {
            registerRawListener(
                RegisteredListener(
                    gameListener = listener,
                    ignoreCancelled = false,
                    ignoreSpectators = false,
                    priority = 50,
                ),
                type,
            )
        }

        for (task in feature.getTasks()) {
            schedule(task)
        }

        features.add(feature)

        return feature
    }

    fun registerRawListener(
        registeredListener: RegisteredListener,
        type: KClass<*>,
    ) {
        registeredListeners
            .getOrPut(type) { mutableListOf() }
            .add(registeredListener)

        game.eventManager.registerListener(game, type, registeredListener)
    }

    inline fun <reified T : Any> registerListener(
        gameListener: TypedGameListener<T>,
    ) {
        registerRawListener(
            RegisteredListener(
                gameListener = gameListener,
                ignoreCancelled = false,
                ignoreSpectators = false,
                priority = 0,
            ),
            T::class,
        )
    }

    inline fun <reified T : Any> registerListeners(
        vararg listeners: TypedGameListener<T>,
    ) {
        listeners.forEach { registerListener(it) }
    }

    fun registerAll(any: Any) {
        val scan = scan(any).getOrThrow { Exception("") }

        scan.forEach { (type, listener) ->
            registerRawListener(listener, type)
        }
    }

    fun schedule(gameTask: GameTask): GameTask {
        game.scheduler.schedule(this, gameTask)
        return gameTask
    }

    fun endStage() {
        game.startNextStage()
    }

    fun endStage(stageCreator: StageCreator<ARENA, TEAM>) = game.startNextStage(stageCreator)

    internal fun unregisterListeners() {
        registeredListeners.forEach { (type, listeners) ->
            listeners.forEach { listener ->
                game.eventManager.unregisterListener(game, type, listener)
            }
        }
    }
}
