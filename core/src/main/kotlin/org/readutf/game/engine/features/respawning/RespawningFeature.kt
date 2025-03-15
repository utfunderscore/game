package org.readutf.game.engine.game.features.respawning

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.Game
import org.readutf.game.engine.event.impl.GameArenaChangeEvent
import org.readutf.game.engine.event.impl.GameJoinEvent
import org.readutf.game.engine.event.impl.GameRespawnEvent
import org.readutf.game.engine.event.impl.StageStartEvent
import org.readutf.game.engine.event.listener.GameListener
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.features.Feature
import org.readutf.game.engine.features.respawning.RespawnHandler
import org.readutf.game.engine.utils.Position
import org.readutf.game.engine.world.GameWorld
import java.util.UUID
import kotlin.reflect.KClass

abstract class RespawningFeature(
    val game: Game<*, *>,
    private val respawnHandler: RespawnHandler,
    private val roundStart: Boolean = true,
    private val arenaChange: Boolean = true,
    private val playerJoin: Boolean = true,
) : Feature() {

    private val logger = KotlinLogging.logger { }

    private val roundStartRespawn = TypedGameListener<StageStartEvent> { stageEvent ->
        logger.info { "Respawning players ${stageEvent.game.getOnlinePlayers()}" }
        for (onlinePlayer in stageEvent.game.getOnlinePlayers()) {
            logger.info { "Respawning player $onlinePlayer" }

            val respawnLocation = respawnHandler.findRespawnLocation(onlinePlayer)
            teleport(onlinePlayer, gameWorld = stageEvent.game.arena!!.instance, respawnLocation)
        }
    }

    private val gameJoinRespawn = TypedGameListener<GameJoinEvent> { stageEvent ->
        logger.info { "Respawning players" }

        val onlinePlayer = stageEvent.player

        logger.info { "Respawning player $onlinePlayer" }

        val respawnLocation = respawnHandler.findRespawnLocation(onlinePlayer)
        teleport(onlinePlayer, gameWorld = stageEvent.game.arena!!.instance, respawnLocation)
    }

    private val arenaChangeRespawn = TypedGameListener<GameArenaChangeEvent> { stageEvent ->
        logger.info { "Respawning players ${stageEvent.game.getOnlinePlayers()}" }
        for (onlinePlayer in stageEvent.game.getOnlinePlayers()) {
            logger.info { "Respawning player $onlinePlayer" }

            val respawnLocation = respawnHandler.findRespawnLocation(onlinePlayer)
            teleport(onlinePlayer, gameWorld = stageEvent.game.arena!!.instance, respawnLocation)
        }
    }

    fun respawn(playerId: UUID): Result<Unit, Throwable> {
        val respawnLocation = respawnHandler.findRespawnLocation(playerId)

        val event =
            game.callEvent(
                GameRespawnEvent(
                    game = game,
                    playerId = playerId,
                    world = game.arena!!.instance,
                    respawnLocation = respawnLocation,
                ),
            )
        if (event.isCancelled()) return Err(Exception("Respawn event was cancelled"))

        teleport(playerId, event.world, event.respawnLocation)

        return Ok(Unit)
    }

    override fun getListeners(): Map<KClass<*>, GameListener> {
        val listeners = mutableMapOf<KClass<*>, GameListener>()
        if (roundStart) {
            listeners[StageStartEvent::class] = roundStartRespawn
        }
        if (playerJoin) {
            listeners[GameJoinEvent::class] = gameJoinRespawn
        }
        if (arenaChange) {
            listeners[GameArenaChangeEvent::class] = arenaChangeRespawn
        }

        return listeners
    }

    abstract fun teleport(playerId: UUID, gameWorld: GameWorld, position: Position)
}
