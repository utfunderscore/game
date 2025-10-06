package org.readutf.engine.event.impl.game;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;

public class GameServerJoinEvent implements GameEvent {

    @NotNull
    private final Game<?,?,?> game;

    public GameServerJoinEvent(@NotNull Game<?, ?, ?> game) {
        this.game = game;
    }

    @Override
    public @NotNull Game<?, ?, ?> getGame() {
        return game;
    }
}
