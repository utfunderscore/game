package org.readutf.engine.event;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;

public interface GameEvent {

    @NotNull Game<?, ?, ?> getGame();
}
