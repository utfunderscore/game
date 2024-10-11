package org.readutf.game.engine.features.deaths

import net.minestom.server.entity.Player
import net.minestom.server.event.entity.EntityDamageEvent
import org.readutf.game.engine.event.annotation.EventListener
import org.readutf.game.engine.features.spectator.SpectatorManager
import org.readutf.game.engine.stage.Stage

class DeathManager(
    val stage: Stage,
    val spectatorManager: SpectatorManager,
    val dropItems: Boolean = false,
) {
    init {
        stage.registerAll(this)
    }

    fun killPlayer(player: Player) {
        if (spectatorManager.isSpectator(player)) return

        if (dropItems) {
            // TODO
        }

        spectatorManager.setSpectator(player)
    }

    @EventListener
    fun onDeath(e: EntityDamageEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (player.health - e.damage.amount <= 0) {
            e.isCancelled = true
            killPlayer(player)
        }
    }
}
