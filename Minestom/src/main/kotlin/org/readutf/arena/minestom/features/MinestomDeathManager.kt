package org.readutf.arena.minestom.features

import net.minestom.server.entity.Player
import net.minestom.server.event.entity.EntityDamageEvent
import org.readutf.arena.minestom.platform.toArenaPlayer
import org.readutf.game.engine.feature.deaths.DeathManager
import org.readutf.game.engine.feature.spectator.SpectatorManager
import org.readutf.game.engine.platform.Platform
import org.readutf.game.engine.stage.GenericStage

class MinestomDeathManager(
    platform: Platform<*>,
    stage: GenericStage,
    spectatorManager: SpectatorManager,
    dropItems: Boolean = false,
) : DeathManager(platform, stage, spectatorManager, dropItems) {

    init {
        stage.registerListener<EntityDamageEvent> { e ->
            val player = e.entity
            if (player is Player) {
                if (player.health - e.damage.amount <= 0) {
                    e.isCancelled = true
                    killPlayer(player.toArenaPlayer(), e.damage.type.name())
                }
            }
        }
    }
}

fun GenericStage.addDeathManager(spectatorManager: SpectatorManager): DeathManager = MinestomDeathManager(game.platform, this, spectatorManager)
