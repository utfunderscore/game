package org.readutf.game.engine.stage

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getOrElse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.Game
import org.readutf.game.engine.arena.Arena
import org.readutf.game.engine.event.annotation.scan
import org.readutf.game.engine.event.listener.RegisteredListener
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.platform.world.ArenaWorld
import org.readutf.game.engine.schedular.GameTask
import org.readutf.game.engine.team.GameTeam
import org.readutf.game.engine.utils.SResult
import kotlin.reflect.KClass

abstract class Stage<WORLD : ArenaWorld, ARENA : Arena<*, WORLD>, TEAM : GameTeam>(
    open val game: Game<WORLD, ARENA, TEAM>,
    val previousStage: Stage<WORLD, ARENA, TEAM>?,
) {

    private val logger = KotlinLogging.logger { }

    val startTime = System.currentTimeMillis()
    val registeredListeners = LinkedHashMap<KClass<*>, MutableList<RegisteredListener>>()

    open fun onStart(): SResult<Unit> = Ok(Unit)

    open fun onFinish(): SResult<Unit> = Ok(Unit)

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
        priority: Int = 50,
        gameListener: TypedGameListener<T>,
    ) {
        registerRawListener(
            RegisteredListener(
                gameListener = gameListener,
                ignoreCancelled = false,
                ignoreSpectators = false,
                priority = priority,
            ),
            T::class,
        )
    }

    fun registerAll(any: Any) {
        val scan = scan(any).getOrElse {
            logger.error { "Failed to scan for event listeners" }
            return
        }

        scan.forEach { (type, listener) ->
            registerRawListener(listener, type)
        }
    }

    fun schedule(gameTask: GameTask): GameTask {
        game.scheduler.schedule(this, gameTask)
        return gameTask
    }

    fun endStage() = game.startNextStage()

    fun endStage(stageCreator: StageCreator<WORLD, ARENA, TEAM>) = game.startNextStage(stageCreator)

    internal fun unregisterListeners() {
        registeredListeners.forEach { (type, listeners) ->
            listeners.forEach { listener ->
                game.eventManager.unregisterEvent(game, type, listener)
            }
        }
    }
}
