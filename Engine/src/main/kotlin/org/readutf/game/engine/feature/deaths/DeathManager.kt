package org.readutf.game.engine.feature.deaths

import io.github.oshai.kotlinlogging.KotlinLogging
import org.readutf.game.engine.event.impl.GameDeathEvent
import org.readutf.game.engine.feature.spectator.SpectatorManager
import org.readutf.game.engine.platform.Platform
import org.readutf.game.engine.platform.player.GamePlayer
import org.readutf.game.engine.stage.GenericStage

abstract class DeathManager(
    private val platform: Platform<*>,
    private val stage: GenericStage,
    private val spectatorManager: SpectatorManager,
    private val dropItems: Boolean = false,
) {
    private val logger = KotlinLogging.logger {}

    fun killPlayer(
        player: GamePlayer,
        damageType: String,
    ) {
        if (spectatorManager.isSpectator(player)) return

        logger.info { "Player ${player.getName()} has died" }

        if (dropItems) {
            // TODO
        }

        spectatorManager.setSpectator(player)
        stage.game.callEvent(GameDeathEvent(stage.game, player, damageType))
    }
}
