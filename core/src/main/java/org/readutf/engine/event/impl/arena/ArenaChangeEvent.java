package org.readutf.engine.event.impl.arena;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.GameEvent;

@Getter
public class ArenaChangeEvent implements GameEvent {

    private final @NotNull Game<?, ?, ?> game;
    private @NotNull final Arena<?, ?> previousArena;
    private @NotNull final Arena<?, ?> currentArena;

    public ArenaChangeEvent(@NotNull Game<?, ?, ?> game, @NotNull Arena<?, ?> previousArena, @NotNull Arena<?, ?> currentArena) {
        this.game = game;
        this.previousArena = previousArena;
        this.currentArena = currentArena;
    }
}
