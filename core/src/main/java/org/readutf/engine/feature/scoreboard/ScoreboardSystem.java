package org.readutf.engine.feature.scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.feature.System;
import org.readutf.engine.task.GameTask;
import org.readutf.engine.task.impl.RepeatingGameTask;

public class ScoreboardSystem implements System {

    private final @NotNull ScoreboardPlatform scoreboardPlatform;
    private final @NotNull Map<UUID, Scoreboard> scoreboards;

    public ScoreboardSystem(@NotNull ScoreboardPlatform scoreboardPlatform) {
        this.scoreboardPlatform = scoreboardPlatform;
        this.scoreboards = new HashMap<>();
    }

    public void setScoreboard(@NotNull UUID playerId, @NotNull Scoreboard scoreboard) {
        scoreboards.put(playerId, scoreboard);
        scoreboardPlatform.refreshScoreboard(playerId, scoreboard);
    }

    @Override
    public @NotNull List<GameTask> getTasks() {
        return List.of(new Task());
    }

    private class Task extends RepeatingGameTask {
        public Task() {
            super(0, 1);
        }

        @Override
        public void run() {
            for (Map.Entry<UUID, Scoreboard> entry : scoreboards.entrySet()) {
                scoreboardPlatform.refreshScoreboard(entry.getKey(), entry.getValue());
            }
        }
    }
}