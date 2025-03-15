package org.readutf.game.minestom.platform.feature.spectator

import org.readutf.game.engine.GenericGame
import org.readutf.game.minestom.platform.feature.spectator.event.GameSpectateEvent
import org.readutf.game.minestom.utils.pos
import java.time.LocalDateTime
import java.util.UUID

class SpectatorListener(
    private val game: GenericGame,
    private val spectatorManager: SpectatorManager,
) : DamageListener {
    override fun onDamage(
        playerId: UUID,
        finalDamage: Float,
        isCancelled: Boolean,
    ) {
        if (isCancelled || spectatorManager.getHealth(playerId) - finalDamage > 0) return

        val event =
            game.callEvent(
                GameSpectateEvent(
                    game = game,
                    SpectatorData(
                        playerId = playerId,
                        external = false,
                        respawn = true,
                        respawnTime = LocalDateTime.now().plusSeconds(5),
                        position = pos(0, 0, 0),
                    ),
                ),
            )

        spectatorManager.setSpectator(event.spectatorData)
    }
}
