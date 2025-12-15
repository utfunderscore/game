package org.readutf.engine.feature.spectator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.buildformat.types.Position;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the data of a spectator
 *
 * @since 1.0
 */
public class SpectatorData {
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

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public @NotNull LocalDateTime getDeathTime() {
        return deathTime;
    }

    public @Nullable LocalDateTime getRespawnTime() {
        return respawnTime;
    }

    public boolean isCanRespawn() {
        return canRespawn;
    }

    public @NotNull Position getSpectatorPosition() {
        return spectatorPosition;
    }
}