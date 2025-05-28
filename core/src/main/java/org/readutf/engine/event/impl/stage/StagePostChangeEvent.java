package org.readutf.engine.event.impl.stage;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.GameEvent;
import org.readutf.engine.stage.Stage;

@Getter
public class StagePostChangeEvent implements GameEvent {

    private final @NotNull Game<?, ?, ?> game;

    private final @NotNull Stage<?, ?, ?> previousStage;
    private final @NotNull Stage<?, ?, ?> currentStage;

    public StagePostChangeEvent(@NotNull Game<?, ?, ?> game, @NotNull Stage<?, ?, ?> previousStage, @NotNull Stage<?, ?, ?> currentStage) {
        this.game = game;
        this.previousStage = previousStage;
        this.currentStage = currentStage;
    }

    @Override
    public @NotNull Game<?, ?, ?> getGame() {
        return game;
    }
}
