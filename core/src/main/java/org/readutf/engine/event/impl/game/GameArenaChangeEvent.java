package org.readutf.engine.event.impl.game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.GameEvent;

public class GameArenaChangeEvent implements GameEvent {

    private final @NotNull Game<?, ?, ?> game;
    private final @NotNull Arena<?, ?> arena;
    private final @Nullable Arena<?, ?> previousArena;

    public GameArenaChangeEvent(@NotNull Game<?, ?, ?> game, @NotNull Arena<?, ?> arena, @Nullable Arena<?, ?> previousArena) {
        this.game = game;
        this.arena = arena;
        this.previousArena = previousArena;
    }

    @Override
    public @NotNull Game<?, ?, ?> getGame() {
        return game;
    }
}
