package org.readutf.engine.event.impl.game;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;

public class GameServerLeaveEvent implements GameEvent {

    private final Game<?, ?, ?> game;

    public GameServerLeaveEvent(Game<?, ?, ?> game) {
        this.game = game;
    }

    @Override
    public @NotNull Game<?, ?, ?> getGame() {
        return game;
    }
}
