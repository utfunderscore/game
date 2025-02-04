package org.readutf.game.engine.features.respawning

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.event.impl.StageStartEvent
import org.readutf.game.engine.event.listener.TypedGameListener
import org.readutf.game.engine.features.Feature
import org.readutf.game.engine.utils.Position
import java.util.UUID

abstract class RespawningFeature(respawnHandler: RespawnHandler) : Feature() {

    private val logger = KotlinLogging.logger { }

    private val roundStartRespawn = TypedGameListener<StageStartEvent> { stageEvent ->
        logger.info { "Respawning all players for stage change" }
        for (onlinePlayer in stageEvent.game.getOnlinePlayers()) {
            val respawnLocation = respawnHandler.findRespawnLocation()
            teleport(onlinePlayer, respawnLocation)
        }
    }

    init {
        registerListener(roundStartRespawn)
    }

    abstract fun teleport(playerId: UUID, position: Position)
}
