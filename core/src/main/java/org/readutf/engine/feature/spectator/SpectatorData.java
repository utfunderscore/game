package org.readutf.engine.feature.spectator;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.buildformat.common.markers.Position;

/**
 * Represents the data of a spectator
 *
 * @since 1.0
 */
@Getter
@Setter
public final class SpectatorData {
    private @NotNull final UUID playerId;
    private @NotNull final LocalDateTime deathTime;
    private @Nullable LocalDateTime respawnTime;
    private boolean canRespawn;
    private @NotNull Position spectatorPosition;

    private SpectatorData(
            @NotNull UUID playerId,
            @NotNull LocalDateTime deathTime,
            @Nullable LocalDateTime respawnTime,
            boolean canRespawn,
            @NotNull Position spectatorPosition) {
        this.playerId = playerId;
        this.deathTime = deathTime;
        this.respawnTime = respawnTime;
        this.canRespawn = canRespawn;
        this.spectatorPosition = spectatorPosition;
    }

    public static SpectatorData permanent(
            UUID playerId,
            LocalDateTime deathTime,
            Position spectatorPosition) {
        return new SpectatorData(playerId, deathTime, null, false, spectatorPosition);
    }

    public static SpectatorData temporary(
            UUID playerId,
            LocalDateTime deathTime,
            LocalDateTime respawnTime,
            Position spectatorPosition) {
        return new SpectatorData(playerId, deathTime, respawnTime, true, spectatorPosition);
    }

}