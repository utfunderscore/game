package org.readutf.engine.event.impl.arena;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.GameEvent;

public class ArenaChangeEvent implements GameEvent {

    private final @NotNull Game<?, ?, ?> game;
    private @NotNull final Arena<?, ?> previousArena;
    private @NotNull final Arena<?, ?> currentArena;

    public ArenaChangeEvent(@NotNull Game<?, ?, ?> game, @NotNull Arena<?, ?> previousArena, @NotNull Arena<?, ?> currentArena) {
        this.game = game;
        this.previousArena = previousArena;
        this.currentArena = currentArena;
    }

    @Override
    public @NotNull Game<?, ?, ?> getGame() {
        return game;
    }

    public @NotNull Arena<?, ?> getPreviousArena() {
        return previousArena;
    }

    public @NotNull Arena<?, ?> getCurrentArena() {
        return currentArena;
    }
}
