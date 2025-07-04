package org.readutf.engine.stage;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.GameException;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.exceptions.EventDispatchException;
import org.readutf.engine.event.listener.GameListener;
import org.readutf.engine.event.listener.ListenerData;
import org.readutf.engine.event.listener.TypedGameListener;
import org.readutf.engine.feature.System;
import org.readutf.engine.task.GameTask;
import org.readutf.engine.team.GameTeam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a stage or phase in a game's lifecycle.
 *
 * <p>A Stage is bound to a specific game and can reference the previous stage in the sequence.
 * It can register event listeners, add features, schedule tasks, and define startup and shutdown logic.
 *
 * @param <ARENA> the type of Arena used in the game
 * @param <TEAM>  the type of GameTeam participating in the game
 */
public abstract class Stage<WORLD, ARENA extends Arena<WORLD, ?>, TEAM extends GameTeam> {

    private static final Logger logger = LoggerFactory.getLogger(Stage.class);

    protected final Game<WORLD, ARENA, TEAM> game;

    protected final Stage<WORLD, ARENA, TEAM> previousStage;

    private boolean active = false;

    protected final List<System> systems = new ArrayList<>();
    private final Map<Class<?>, List<GameListener>> registeredListeners = new LinkedHashMap<>();

    /**
     * Constructs a new stage associated with the given game and an optional previous stage.
     *
     * @param game          the game instance this stage belongs to
     * @param previousStage the previous stage, or null if this is the first stage
     */
    public Stage(Game<WORLD, ARENA, TEAM> game, Stage<WORLD, ARENA, TEAM> previousStage) {
        this.game = game;
        this.previousStage = previousStage;
    }

    public void start() throws GameException {
        active = true;
        try {
            onStart();
        } catch (Exception e) {
            logger.error("Failed to start stage: {}", e.getMessage(), e);
            throw new GameException("Stage initialization failed", e);
        }
    }

    public void end() throws Exception {
        active = false;
        try {
            onFinish();
        } catch (Exception e) {
            logger.error("Failed to finish stage: {}", e.getMessage(), e);
            throw new GameException("Stage cleanup failed", e);
        }
    }

    /**
     * Called when the stage begins. Subclasses can override to provide startup logic.
     *
     * @throws Exception if initialization fails
     */
    protected void onStart() throws Exception {
        // Default no-op
    }

    /**
     * Called when the stage ends. Subclasses can override to provide cleanup logic.
     *
     * @throws Exception if cleanup fails
     */
    protected void onFinish() throws Exception {
        // Default no-op
    }

    /**
     * Adds a feature to the stage. All its listeners are registered and tasks scheduled.
     *
     * @param feature the feature to add
     * @param <T>     the feature type
     * @return the added feature
     */
    public <T extends System> @NotNull T addSystem(@NotNull T feature) {

        for (ListenerData listener : feature.getListeners()) {
            try {
                registerRawListener(listener.getGameListener(), listener.getType());
            } catch (EventDispatchException e) {
                logger.error("Failed to register listener: {}", e.getMessage());
            }
        }

        for (GameTask task : feature.getTasks()) {
            schedule(task);
        }

        systems.add(feature);
        return feature;
    }

    /**
     * Registers a raw, low-level event listener for a specific event type.
     *
     * @param registeredListener the listener to register
     * @param type               the class of the event
     */
    public void registerRawListener(GameListener registeredListener, Class<?> type) throws EventDispatchException {
        registeredListeners.computeIfAbsent(type, k -> new ArrayList<>()).add(registeredListener);

        game.getEventManager().registerListener(game, ListenerData.of(type, registeredListener));
    }

    /**
     * Registers a strongly typed listener for the given event class.
     *
     * @param gameListener the listener to register
     * @param clazz        the event class
     * @param <T>          the type of the event
     */
    public <T> void registerListener(TypedGameListener<T> gameListener, Class<T> clazz) throws EventDispatchException {
        registerRawListener(gameListener, clazz);
    }

    /**
     * Registers multiple listeners for the same event class.
     *
     * @param clazz     the class of the event
     * @param listeners the listeners to register
     * @param <T>       the event type
     */
    @SafeVarargs
    public final <T> void registerListeners(Class<T> clazz, TypedGameListener<T> @NotNull ... listeners)
            throws EventDispatchException {
        for (TypedGameListener<T> listener : listeners) {
            registerListener(listener, clazz);
        }
    }

    /**
     * Schedules a task to run during this stage.
     *
     * @param task the task to schedule
     * @return the scheduled task
     */
    public GameTask schedule(GameTask task) {
        if (!active) throw new RuntimeException("Cannot schedule a task when the stage is not active");
        game.getScheduler().schedule(this, task);
        return task;
    }

    /**
     * Signals the game to proceed to the next stage.
     */
    public void endStage() throws GameException {
        game.startNextStage();
    }

    /**
     * Signals the game to proceed to a specific next stage, defined by a factory.
     *
     * @param stageCreator the factory that creates the next stage
     */
    public void endStage(StageCreator<WORLD, ARENA, TEAM> stageCreator) throws GameException {
        game.startNextStage(stageCreator);
    }

    /**
     * Unregisters all listeners registered during this stage.
     * Called internally by the engine when the stage ends.
     */
    public void unregisterListeners() {
        for (Map.Entry<Class<?>, List<GameListener>> entry : registeredListeners.entrySet()) {
            for (GameListener listener : entry.getValue()) {
                game.getEventManager().unregisterListener(game, entry.getKey(), listener);
            }
        }
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * @return the features associated with this stage
     */
    public @NotNull List<System> getSystems() {
        return systems;
    }

    public Game<WORLD, ARENA, TEAM> getGame() {
        return game;
    }

    public Stage<WORLD, ARENA, TEAM> getPreviousStage() {
        return previousStage;
    }
}
