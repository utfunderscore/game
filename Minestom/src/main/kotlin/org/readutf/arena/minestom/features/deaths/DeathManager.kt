package org.readutf.arena.minestom.features.deaths

import io.github.oshai.kotlinlogging.KotlinLogging
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.registry.DynamicRegistry
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.event.impl.GameDeathEvent
import org.readutf.game.engine.features.spectator.SpectatorManager
import org.readutf.game.engine.stage.GenericStage

class DeathManager(
    val stage: GenericStage,
    val spectatorManager: SpectatorManager,
    val dropItems: Boolean = false,
) {
    private val logger = KotlinLogging.logger {}

    init {
        stage.registerAll(this)
    }

    fun killPlayer(
        player: Player,
        damageType: DynamicRegistry.Key<DamageType>,
    ) {
        if (spectatorManager.isSpectator(player)) return

        logger.info { "Player ${player.username} has died" }

        if (dropItems) {
            // TODO
        }

        spectatorManager.setSpectator(player)
        stage.game.callEvent(GameDeathEvent(stage.game, player, damageType))
    }

    @EventListener
    fun onDeath(e: EntityDamageEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (player.health - e.damage.amount <= 0) {
            e.isCancelled = true
            killPlayer(player, e.damage.type)
        }
    }
}
