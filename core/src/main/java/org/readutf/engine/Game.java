package org.readutf.engine;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.GameEventManager;
import org.readutf.engine.stage.Stage;
import org.readutf.engine.stage.StageCreator;
import org.readutf.engine.task.GameScheduler;
import org.readutf.engine.team.GameTeam;

import java.util.LinkedList;
import java.util.UUID;

public class Game<ARENA extends Arena<?, ?>, TEAM extends GameTeam> {

    private final @NotNull UUID gameId;
    private @NotNull final LinkedList<StageCreator<ARENA, TEAM>> stageCreators;
    private @NotNull final GameScheduler gameScheduler;
    private @Getter @NotNull final GameEventManager eventManager;
    private @Nullable Stage<ARENA, TEAM> currentStage;

    public Game(
            @NotNull LinkedList<StageCreator<ARENA, TEAM>> stageCreators,
            @NotNull GameScheduler gameScheduler,
            @NotNull GameEventManager eventManager) {
        this.stageCreators = stageCreators;
        this.gameScheduler = gameScheduler;
        this.eventManager = eventManager;
        this.gameId = UUID.randomUUID();
    }

    public @NotNull UUID getGameId() {
        return gameId;
    }

    public void startNextStage(StageCreator<ARENA, TEAM> stageCreator) {
        stageCreator.startNextStage(currentStage);
    }

    public void startNextStage() {
        stageCreators.getFirst().startNextStage(currentStage);
    }

    public GameScheduler getScheduler() {
        return gameScheduler;
    }
}
