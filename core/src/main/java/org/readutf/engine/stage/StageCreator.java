package org.readutf.engine.stage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.stage.exception.StageChangeException;
import org.readutf.engine.team.GameTeam;

public interface StageCreator<ARENA extends Arena<?, ?>, TEAM extends GameTeam> {

    @NotNull
    Stage<ARENA, TEAM> startNextStage(@NotNull Game<ARENA, TEAM> game, @Nullable Stage<ARENA, TEAM> previousStage) throws StageChangeException;
}
