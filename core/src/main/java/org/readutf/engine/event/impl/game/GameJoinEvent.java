package org.readutf.engine.event.impl.game;

import java.util.UUID;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;

@Getter
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
}
