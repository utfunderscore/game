package org.readutf.engine.feature.spectator;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;

@Getter
public class GameSpectateEvent implements GameEvent {

    private final @NotNull Game<?, ?, ?> game;
    private final @NotNull SpectatorData spectatorData;

    public GameSpectateEvent(@NotNull Game<?, ?, ?> game, @NotNull SpectatorData spectatorData) {
        this.game = game;
        this.spectatorData = spectatorData;
    }

    @Override
    public @NotNull Game<?, ?, ?> getGame() {
        return game;
    }
}
