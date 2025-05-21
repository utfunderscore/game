package org.readutf.engine;



import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.arena.Arena;
import org.readutf.engine.event.GameEvent;
import org.readutf.engine.event.GameEventManager;
import org.readutf.engine.event.exceptions.EventDispatchException;
import org.readutf.engine.event.impl.game.*;
import org.readutf.engine.event.impl.stage.StageStartEvent;
import org.readutf.engine.event.listener.GameListener;
import org.readutf.engine.event.listener.TypedGameListener;
import org.readutf.engine.feature.Feature;
import org.readutf.engine.stage.Stage;
import org.readutf.engine.stage.StageCreator;
import org.readutf.engine.task.GameScheduler;
import org.readutf.engine.task.GameTask;
import org.readutf.engine.team.GameTeam;
import org.readutf.engine.team.TeamSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Core game class that manages the game lifecycle, players, teams, stages, and events.
 * Handles transitions between game states and coordinates game-related operations.
 *
 * @param <ARENA> The type of arena used in the game
 * @param <TEAM> The type of team used in the game
 */
public abstract class Game<ARENA extends Arena<?, ?>, TEAM extends GameTeam> {

    private static final AtomicInteger idGenerator = new AtomicInteger();

    // Logger for this class
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    // Game unique identifier
    @NonNull
    private final UUID gameId;
    
    // Game scheduler for managing timed tasks
    @NonNull
    private final GameScheduler scheduler;
    
    // Event manager for handling game events
    @NonNull
    private final GameEventManager eventManager;
    
    // Team selector for assigning players to teams
    @NonNull
    private TeamSelector<TEAM> teamSelector;
    
    // Queue of stage creators for the game progression
    @NonNull
    private final Deque<StageCreator<ARENA, TEAM>> stageCreators = new ArrayDeque<>();
    
    // Current active stage
    @Nullable
    private Stage<ARENA, TEAM> currentStage;
    
    // Current active arena
    @Nullable
    private ARENA arena;
    
    // Map of team names to team instances
    @NonNull
    private final LinkedHashMap<String, TEAM> teams = new LinkedHashMap<>();
    
    // Current state of the game
    @NonNull
    private GameState gameState = GameState.STARTUP;
    
    // List of active features for the game
    @NonNull
    private final List<Feature> features = new ArrayList<>();

    /**
     * Creates a new game instance with the specified components
     *
     * @param scheduler Game scheduler for timing tasks
     * @param eventManager Event system for the game
     * @param teamSelector Team assignment strategy
     */
    protected Game(
            @NonNull GameScheduler scheduler,
            @NonNull GameEventManager eventManager,
            @NonNull TeamSelector<TEAM> teamSelector
    ) {
        this.scheduler = scheduler;
        this.eventManager = eventManager;
        this.teamSelector = teamSelector;
        this.gameId = UUID.randomUUID();
    }

    /**
     * Registers a new team to the game
     *
     * @param team Team to register
     * @throws Exception if a team with the same name already exists
     */
    public void registerTeam(@NonNull TEAM team) throws Exception {
        String teamName = team.getTeamName();
        logger.info("Adding team {} to game ({})", teamName, gameId);
        
        if (teams.containsKey(teamName)) {
            logger.error("Team already exists with the name {}", teamName);
            throw new Exception("Team already exists with the name " + teamName);
        }

        teams.put(teamName, team);
    }

    /**
     * Starts the game by initializing the first stage
     * 
     * @throws Exception if game is not in startup state or no arena is active
     */
    public void start() throws Exception {
        logger.info("Starting game");
        
        if (gameState != GameState.STARTUP) {
            logger.error("Game is not in startup state");
            throw new Exception("Game is not in startup state");
        }
        
        startNextStage();

        if (arena == null) {
            logger.error("No arena is active");
            throw new Exception("No arena is active");
        }

        if (currentStage != null) {
            currentStage.onStart();
        }
        
        gameState = GameState.ACTIVE;
    }

    /**
     * Starts the next stage in the game sequence
     * 
     * @return The new active stage
     * @throws Exception if no more stages are available
     */
    @NonNull
    public Stage<ARENA, TEAM> startNextStage() throws Exception {
        StageCreator<ARENA, TEAM> nextStageCreator = stageCreators.pollFirst();
        if (nextStageCreator == null) {
            logger.error("No more stages to start");
            throw new Exception("No more stages to start");
        }

        return startNextStage(nextStageCreator);
    }

    /**
     * Starts a specific stage using the provided creator
     * 
     * @param nextStageCreator Creator for the next stage
     * @return The new active stage
     * @throws Exception if stage creation or initialization fails
     */
    @NonNull
    public Stage<ARENA, TEAM> startNextStage(@NonNull StageCreator<ARENA, TEAM> nextStageCreator) throws Exception {
        logger.info("Starting next stage...");

        if (currentStage != null) {
            currentStage.unregisterListeners();
            currentStage.onFinish();
            
            for (Feature feature : currentStage.getFeatures()) {
                System.out.println("Shutting down feature " + feature.getClass().getSimpleName());
                feature.shutdown();
            }
        }

        Stage<ARENA, TEAM> previous = currentStage;
        Stage<ARENA, TEAM> nextStage = nextStageCreator.startNextStage(previous);

        logger.info("Starting stage {}", nextStage.getClass().getSimpleName());

        currentStage = nextStage;

        callEvent(new StageStartEvent(this, nextStage, previous));

        nextStage.onStart();

        return currentStage;
    }

    /**
     * Ends the game and performs cleanup
     * 
     * @throws Exception if game is not in active state
     */
    public void end() throws Exception {
        callEvent(new GameEndEvent(this));

        if (gameState != GameState.ACTIVE) {
            logger.error("GameState is not active");
            throw new Exception("GameState is not active");
        }

        if (arena != null) {
            //TODO: Free the arena
        }

        scheduler.cancelGameTasks(this);
        eventManager.shutdown(this);

        for (Feature feature : features) {
            System.out.println("Shutting down feature " + feature.getClass().getSimpleName());
            feature.shutdown();
        }
        features.clear();
    }

    /**
     * Schedules a task to run during this game
     * 
     * @param gameTask Task to schedule
     */
    public void schedule(@NonNull GameTask gameTask) {
        scheduler.schedule(this, gameTask);
    }

    /**
     * Handles catastrophic game failure by cleaning up resources
     * 
     * @param cause Optional cause of the crash
     * @throws Exception always thrown after cleanup
     */
    public void crash(@Nullable Throwable cause) throws Exception {
        callEvent(new GameCrashEvent(this));

        if (arena != null) {
            //TODO: Free arena on crash
//            arena.free();
        }

        String reason = cause != null ? cause.toString() : "Unknown Reason";
        logger.error("Game " + gameId + " crashed: " + reason);

        throw new Exception("Game crashed");
    }

    /**
     * Adds a feature to the game
     * 
     * @param feature Feature to add
     * @param <T> Type of feature
     * @return The added feature
     */
    @NonNull
    public <T extends Feature> T addFeature(@NonNull T feature) {
        features.add(feature);

        for (Map.Entry<Class<?>, GameListener> listenerEntry : feature.getListeners().entrySet()) {
            eventManager.registerListener(this, listenerEntry.getKey(), listenerEntry.getValue());
        }

//        for (Map.Entry<Class<?>, GameListener> entry : feature.getListeners()) {
//            eventManager.registerListener(
//                this,
//
//            );
//        }

        for (GameTask task : feature.getTasks()) {
            scheduler.schedule(this, task);
        }

        return feature;
    }

    /**
     * Gets a feature by its class
     * 
     * @param clazz Class of the feature
     * @param <T> Type of feature
     * @return The feature or null if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Feature> T getFeature(@NonNull Class<? extends T> clazz) {
        // First check game features
        for (Feature feature : features) {
            if (feature.getClass().equals(clazz)) {
                return (T) feature;
            }
        }
        
        // Then check stage features
        if (currentStage != null) {
            for (Feature feature : currentStage.getFeatures()) {
                if (feature.getClass().equals(clazz)) {
                    return (T) feature;
                }
            }
        }

        return null;
    }

    /**
     * Registers an event listener
     * 
     * @param eventClass Class of event to listen for
     * @param typedGameListener Listener implementation
     * @param <T> Event type
     */
    public <T> void registerListener(@NonNull Class<T> eventClass, @NonNull TypedGameListener<T> typedGameListener) {
        eventManager.registerListener(
            this,
            eventClass,
            typedGameListener
        );
    }

    /**
     * Changes the current arena
     * 
     * @param arena New arena
     */
    public void changeArena(@NonNull ARENA arena) throws EventDispatchException {
        logger.info("Changing to arena " + arena);

        ARENA previousArena = this.arena;
        this.arena = arena;

        callEvent(new GameArenaChangeEvent(this, arena, previousArena));
    }

    /**
     * Registers stages to the game
     * 
     * @param stageCreators Stage creators in sequence
     */
    @SafeVarargs
    public final void registerStage(@NonNull StageCreator<ARENA, TEAM>... stageCreators) {
        Collections.addAll(this.stageCreators, stageCreators);
    }

    /**
     * Dispatches a game event
     * 
     * @param event Event to dispatch
     * @param <T> Event type
     * @return The event after processing
     */
    @NonNull
    public <T extends GameEvent> T callEvent(@NonNull T event) throws EventDispatchException {
        eventManager.callEvent(event, this);
        return event;
    }

    /**
     * Gets all players in the game
     * 
     * @return List of player UUIDs
     */
    @NonNull
    public List<UUID> getPlayers() {
        return teams.values().stream()
                .flatMap(team -> team.getPlayers().stream())
                .collect(Collectors.toList());
    }

    /**
     * Gets all online players in the game
     * 
     * @return List of online player UUIDs
     */
    @NonNull
    public List<UUID> getOnlinePlayers() {
        return getPlayers().stream()
                .filter(this::isOnline)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a player is online
     * 
     * @param playerId Player UUID
     * @return True if online
     */
    public abstract boolean isOnline(@NonNull UUID playerId);

    /**
     * Sends a message to a player
     * 
     * @param playerId Player UUID
     * @param component Message component
     */
    public abstract void messagePlayer(@NonNull UUID playerId, @NonNull Component component);

    /**
     * Adds a player to the game
     * 
     * @param playerId Player UUID
     * @throws Exception if team assignment fails
     */
    public void addPlayer(@NonNull UUID playerId) throws Exception {
        TEAM team = teamSelector.getTeam(playerId);
        logger.info("Adding player " + playerId + " to team " + team.getTeamName());

        team.getPlayers().add(playerId);
        teams.put(team.getTeamName(), team);

        callEvent(new GameJoinEvent(this, playerId));
    }

    /**
     * Removes a player from the game
     * 
     * @param playerId Player UUID
     * @throws Exception if player is not in a team
     */
    public void removePlayer(@NonNull UUID playerId) throws Exception {
        logger.info("Removing player " + playerId);

        TEAM team = getTeam(playerId);
        if (team == null) {
            logger.error("GamePlayer is not in a team");
            throw new Exception("GamePlayer is not in a team");
        }
        
        team.getPlayers().remove(playerId);
        callEvent(new GameLeaveEvent(this, playerId));
    }

    /**
     * Gets a player's team
     * 
     * @param playerId Player UUID
     * @return Team or null if not found
     */
    @Nullable
    public TEAM getTeam(@NonNull UUID playerId) {
        for (TEAM team : teams.values()) {
            if (team.getPlayers().contains(playerId)) {
                return team;
            }
        }
        return null;
    }

    /**
     * Gets a team by name
     * 
     * @param teamName Team name
     * @return Team or null if not found
     */
    @Nullable
    public TEAM getTeam(@NonNull String teamName) {
        for (Map.Entry<String, TEAM> entry : teams.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(teamName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Gets all teams
     * 
     * @return List of teams
     */
    @NonNull
    public List<TEAM> getTeams() {
        return new ArrayList<>(teams.values());
    }

    /**
     * Gets the current arena
     * 
     * @return Current arena
     * @throws Exception if no arena is active
     */
    @NonNull
    public ARENA getArena() throws Exception {
        if (arena == null) {
            logger.error("No arena is active");
            throw new Exception("No arena is active");
        }
        return arena;
    }

    /**
     * Sends a message to all online players
     * 
     * @param component Message component
     */
    public void messageAll(@NonNull Component component) {
        for (UUID onlinePlayer : getOnlinePlayers()) {
            messagePlayer(onlinePlayer, component);
        }
    }

    /**
     * Gets the index of a team
     * 
     * @param team Team to get index for
     * @return Team index
     */
    public int getTeamId(@NonNull GameTeam team) {
        return new ArrayList<>(teams.values()).indexOf(team);
    }
    
    /**
     * Gets the game's unique identifier
     * 
     * @return Game ID
     */
    @NonNull
    public UUID getGameId() {
        return gameId;
    }
    
    /**
     * Gets the game's event manager
     * 
     * @return Event manager
     */
    @NonNull
    public GameEventManager getEventManager() {
        return eventManager;
    }
    
    /**
     * Gets the team selector
     * 
     * @return Team selector
     */
    @NonNull
    public TeamSelector<TEAM> getTeamSelector() {
        return teamSelector;
    }
    
    /**
     * Sets the team selector
     * 
     * @param teamSelector New team selector
     */
    public void setTeamSelector(@NonNull TeamSelector<TEAM> teamSelector) {
        this.teamSelector = teamSelector;
    }
    
    /**
     * Gets the current stage
     * 
     * @return Current stage or null if no stage is active
     */
    @Nullable
    public Stage<ARENA, TEAM> getCurrentStage() {
        return currentStage;
    }
    
    /**
     * Sets the current stage
     * 
     * @param currentStage New current stage
     */
    public void setCurrentStage(@Nullable Stage<ARENA, TEAM> currentStage) {
        this.currentStage = currentStage;
    }
    
    /**
     * Gets the current game state
     * 
     * @return Game state
     */
    @NonNull
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Sets the game state
     * 
     * @param gameState New game state
     */
    public void setGameState(@NonNull GameState gameState) {
        this.gameState = gameState;
    }
}