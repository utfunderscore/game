package org.readutf.game.minestom.platform.feature.spectator

import org.readutf.buildformat.common.markers.Position
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents the data of a spectator
 * @param playerId the player id
 * @param external if the spectator is not part of the game (a viewer)
 * @param respawn if the spectator should respawn
 * @param respawnTime the time when the player should respawn
 * @param position the position where the player should respawn
 * @since 1.0
 */
class SpectatorData(
    var playerId: UUID,
    var external: Boolean = false,
    var respawn: Boolean = false,
    val deathTime: LocalDateTime = LocalDateTime.now(),
    var respawnTime: LocalDateTime = LocalDateTime.now(),
    var position: Position = Position(0.0, 0.0, 0.0),
)
