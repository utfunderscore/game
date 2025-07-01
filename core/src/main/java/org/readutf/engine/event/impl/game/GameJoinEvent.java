package org.readutf.engine.event.impl.game;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;

import java.util.UUID;

public class GameJoinEvent implements GameEvent {

    private final @NotNull Game<?, ?, ?> game;
    private final @NotNull UUID playerId;

    public GameJoinEvent(@NotNull Game<?, ?, ?> game, @NotNull UUID playerId) {
        this.game = game;
        this.playerId = playerId;
    }

    @Override
    public @NotNull Game<?, ?, ?> getGame() {
        return game;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }
}
