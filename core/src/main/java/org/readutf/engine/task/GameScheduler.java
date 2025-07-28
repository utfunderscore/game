package org.readutf.engine.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameScheduler {

    private static final Logger logger = LoggerFactory.getLogger(GameScheduler.class);

    private final GameSchedulerPlatform platform;
    private final Map<UUID, List<GameTask>> pendingTasks = new HashMap<>();
    private final Map<@NotNull UUID, @NotNull List<@NotNull GameTask>> gameTasks = new HashMap<>();

    public GameScheduler(GameSchedulerPlatform platform) {
        this.platform = platform;
        platform.scheduleRepeatingTask(this::tick);
    }

    public void tick() {

        for (Map.Entry<@NotNull UUID, @NotNull List<@NotNull GameTask>> uuidListEntry : pendingTasks.entrySet()) {
            gameTasks.computeIfAbsent(uuidListEntry.getKey(), k -> new ArrayList<>()).addAll(uuidListEntry.getValue());
        }
        pendingTasks.clear();

        Map<UUID, List<GameTask>> remaining = new HashMap<>();
        for (Map.Entry<UUID, List<GameTask>> entry : gameTasks.entrySet()) {
            logger.debug("Ticking tasks for game {}", entry.getKey());

            List<GameTask> remainingTasks = new ArrayList<>();
            List<GameTask> startingTasks = entry.getValue();
            for (@NotNull GameTask task : startingTasks) {
                try {
                    task.tick();
                } catch (Exception e) {
                    logger.error("Error while executing task {} for game {}", task, entry.getKey(), e);
                    continue; // Skip to the next task
                }
                if (!task.isMarkedForRemoval()) {
                    remainingTasks.add(task);
                    logger.debug("Task {} for game {} is still active", task, entry.getKey());
                } else {
                    logger.debug("Task {} for game {} is marked for removal", task, entry.getKey());
                }
            }
            if(!remainingTasks.isEmpty()) {
                remaining.put(entry.getKey(), remainingTasks);
            }
        }
        gameTasks.clear();
        for (Map.Entry<@NotNull UUID, @NotNull List<@NotNull GameTask>> uuidListEntry : remaining.entrySet()) {
            gameTasks.computeIfAbsent(uuidListEntry.getKey(), k -> new ArrayList<>()).addAll(uuidListEntry.getValue());
        }
    }

    /**
     * Schedules a task tied to a specific game instance.
     *
     * @param game     the game to associate the task with
     * @param gameTask the task to schedule
     */
    public void schedule(@NotNull Game<?, ?, ?> game, @NotNull GameTask gameTask) {
        platform.executeTask(() -> {
            logger.info("Scheduling task {} for game {}", gameTask, game.getId());
            List<@NotNull GameTask> tasks = pendingTasks.getOrDefault(game.getId(), new ArrayList<>());
            tasks.add(gameTask);
            pendingTasks.put(game.getId(), tasks);
        });
    }

    /**
     * Schedules a task tied to a specific stage. The task only runs if the stage is still active.
     *
     * @param stage    the stage to bind the task to
     * @param gameTask the task to execute
     */
    public void schedule(@NotNull Stage<?, ?, ?> stage, @NotNull GameTask gameTask) {
        GameTask wrapped = new GameTask() {
            @Override
            public void tick() {
                if (stage.getGame().getCurrentStage() == stage) {
                    gameTask.tick();
                }
            }

            @Override
            public boolean isMarkedForRemoval() {
                return gameTask.isMarkedForRemoval() || markedForRemoval /* || stage.getGame().getCurrentStage() != stage*/;
            }

            @Override
            public String toString() {
                return "WrappedGameTask{" +
                        "originalTask=" + gameTask +
                        ", stage=" + stage.getGame().getCurrentStage() +
                        '}';
            }
        };

        schedule(stage.getGame(), wrapped);
    }

    /**
     * Cancels all scheduled tasks for a given game by marking them for removal.
     *
     * @param game the game whose tasks should be cancelled
     */
    public void cancelGameTasks(@NotNull Game<?, ?, ?> game) {
        logger.info("Cancelling tasks for game {}", game.getId());

        List<GameTask> tasks = gameTasks.get(game.getId());
        if (tasks != null) {
            for (GameTask task : tasks) {
                task.markForRemoval();
            }
        }
    }
}
