package org.readutf.engine.minestom.schedular;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.readutf.engine.Game;
import org.readutf.engine.stage.Stage;
import org.readutf.engine.task.impl.RepeatingGameTask;

/**
 * CountdownTask is a repeating game task that executes callbacks at specific intervals
 * during a countdown period. It tracks the remaining time and triggers the provided
 * executor function when certain time thresholds are reached.
 */
@Slf4j
public abstract class CountdownTask extends RepeatingGameTask {

    /**
     * -- GETTER --
     *  Gets the start time of this countdown.
     *
     * @return The LocalDateTime when this countdown started
     */
    @Getter private final LocalDateTime start;
    /**
     * -- GETTER --
     *  Gets the end time of this countdown.
     *
     * @return The LocalDateTime when this countdown will expire
     */
    @Getter private final LocalDateTime endTime;
    private final List<Integer> remainingIntervals;

    /**
     * Creates a new CountdownTask that will run for the specified duration.
     *
     * @param duration The total duration of the countdown
     * @param intervals List of time intervals (in milliseconds) at which to execute callbacks
     */
    public CountdownTask(Duration duration, List<Integer> intervals) {
        super(0, 1); // Start immediately, repeat every tick

        this.start = LocalDateTime.now();
        this.endTime = start.plus(duration);
        this.remainingIntervals = new ArrayList<>(intervals); // Create mutable copy
    }

    public abstract void handleInterval(int interval);

    /**
     * Gets the remaining time in milliseconds until the countdown expires.
     *
     * @return The time left in milliseconds, or 0 if the countdown has expired
     */
    public long getTimeLeftMilliseconds() {
        return Duration.between(LocalDateTime.now(), endTime).toMillis();
    }

    /**
     * Executes the countdown logic on each tick.
     * Checks if the countdown has expired or if any intervals should be triggered.
     */
    @Override
    public void run() {

        // Check if the countdown has expired
        if (LocalDateTime.now().isAfter(endTime) || LocalDateTime.now().isEqual(endTime)) {
            log.info("Countdown expired, executing final callback. {}", isMarkedForRemoval());
            markForRemoval(); // Stop the task
            try {
                handleInterval(0); // Execute final callback with 0
            } catch (Exception e) {
                log.error("Error executing final callback for CountdownTask", e);
            }
            return;
        }

        // Check which intervals should be triggered based on remaining time
        long timeLeft = getTimeLeftMilliseconds();
        // Create a copy to avoid concurrent modification during iteration
        List<Integer> intervalsToCheck = new ArrayList<>(remainingIntervals);

        for (Integer interval : intervalsToCheck) {
            if (timeLeft <= (interval * 1000L)) {
                handleInterval(interval); // Execute callback for this interval
                remainingIntervals.remove(interval); // Remove interval so it doesn't trigger again
            }
        }
    }

    /**
     * Starts a countdown task on the given game with a duration in milliseconds.
     *
     * @param game The GenericGame to schedule the countdown on
     * @param durationMillis The duration of the countdown in milliseconds
     * @param intervals List of time intervals (in milliseconds) at which to execute callbacks
     * @param intervalExecutor Function to execute when an interval is reached
     */
    public static void startCountdown(
            Game<?, ?, ?> game, long durationMillis, List<Integer> intervals, Consumer<Integer> intervalExecutor) {
        Duration duration = Duration.ofMillis(durationMillis);
        CountdownTask task = new CountdownTask(duration, intervals) {
            @Override
            public void handleInterval(int interval) {
                intervalExecutor.accept(interval); // Execute the provided callback
            }
        };
        game.schedule(task);
    }

    /**
     * Starts a countdown task on the given game with a Duration object.
     *
     * @param game The GenericGame to schedule the countdown on
     * @param duration The duration of the countdown
     * @param intervals List of time intervals (in milliseconds) at which to execute callbacks
     * @param intervalExecutor Function to execute when an interval is reached
     */
    public static void startCountdown(
            Game<?, ?, ?> game, Duration duration, List<Integer> intervals, Consumer<Integer> intervalExecutor) {
        CountdownTask task = new CountdownTask(duration, intervals) {
            @Override
            public void handleInterval(int interval) {
                intervalExecutor.accept(interval); // Execute the provided callback
            }
        };
        game.schedule(task);
    }

    /**
     * Starts a countdown task on the given stage with a duration in milliseconds.
     *
     * @param stage The GenericStage to schedule the countdown on
     * @param durationMillis The duration of the countdown in milliseconds
     * @param intervals List of time intervals (in milliseconds) at which to execute callbacks
     * @param intervalExecutor Function to execute when an interval is reached
     */
    public static void startCountdown(
            Stage<?, ?, ?> stage, long durationMillis, List<Integer> intervals, Consumer<Integer> intervalExecutor) {
        Duration duration = Duration.ofMillis(durationMillis);
        CountdownTask task = new CountdownTask(duration, intervals) {
            @Override
            public void handleInterval(int interval) {
                intervalExecutor.accept(interval); // Execute the provided callback
            }
        };
        stage.schedule(task);
    }

    /**
     * Starts a countdown task on the given stage with a Duration object.
     * Returns the created CountdownTask for further manipulation if needed.
     *
     * @param stage The GenericStage to schedule the countdown on
     * @param duration The duration of the countdown
     * @param intervals List of time intervals (in milliseconds) at which to execute callbacks
     * @param intervalExecutor Function to execute when an interval is reached
     * @return The created CountdownTask instance
     */
    public static CountdownTask startCountdown(
            Stage<?, ?, ?> stage, Duration duration, List<Integer> intervals, Consumer<Integer> intervalExecutor) {
        CountdownTask task = new CountdownTask(duration, intervals) {
            @Override
            public void handleInterval(int interval) {
                intervalExecutor.accept(interval); // Execute the provided callback
            }
        };
        stage.schedule(task);
        return task;
    }
}