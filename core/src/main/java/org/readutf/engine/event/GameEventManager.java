package org.readutf.engine.event;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.event.adapter.EventGameAdapter;
import org.readutf.engine.event.adapter.TypedEventAdapter;
import org.readutf.engine.event.adapter.impl.GameEventAdapter;
import org.readutf.engine.event.defaults.ServerJoinListener;
import org.readutf.engine.event.defaults.ServerLeaveListener;
import org.readutf.engine.event.exceptions.EventAdaptException;
import org.readutf.engine.event.exceptions.EventDispatchException;
import org.readutf.engine.event.listener.GameListener;
import org.readutf.engine.event.listener.ListenerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameEventManager {

    private final Logger logger = LoggerFactory.getLogger(GameEventManager.class.getName());

    @NotNull
    private final GameManager gameManager;
    private final @NotNull GameEventPlatform gameEventPlatform;
    private final Map<Class<?>, EventGameAdapter> eventAdapters = new HashMap<>();
    private final Set<Class<?>> registeredTypes = new HashSet<>();
    private final Set<Class<?>> noAdapters = new HashSet<>();
    private final Map<UUID, List<ListenerData>> gameListeners = new LinkedHashMap<>();
    private final Set<Class<?>> eventStackTraceEnabled = new HashSet<>();

    public GameEventManager(@NotNull GameManager gameManager,  @NotNull GameEventPlatform gameEventPlatform) {
        this.gameManager = gameManager;
        this.gameEventPlatform = gameEventPlatform;
        eventAdapters.put(GameEvent.class, new GameEventAdapter());
        gameEventPlatform.registerAdapters(eventAdapters);
        registerDefaultListeners();
    }

    public void registerDefaultListeners() {
        gameEventPlatform.registerServerJoinListener(new ServerJoinListener(gameManager));
        gameEventPlatform.registerServerLeaveListener(new ServerLeaveListener(gameManager));
    }

    public <T> void registerEventAdapter(Class<T> type, TypedEventAdapter<T> adapter) {
        if (eventAdapters.containsKey(type)) {
            logger.warn("Event adapter for type {} is already registered, replacing it.", type);
        }
        eventAdapters.put(type, adapter);
    }

    public <T> @NotNull T callEvent(@NotNull T event, @NotNull Game<?, ?, ?> game) throws EventDispatchException {
        logger.debug("Calling event: {}", event.getClass().getSimpleName());

        List<ListenerData> listeners = this.gameListeners.get(game.getId());
        if ((listeners == null || listeners.isEmpty())) {
            if (eventStackTraceEnabled.contains(event.getClass())) {
                logger.info(
                        "No game listeners found for event: {}.",
                        event.getClass().getSimpleName());
            }
            return event;
        }

        List<ListenerData> applicableListeners = new ArrayList<>();
        for (ListenerData listener : listeners) {
            if(listener.getType().isAssignableFrom(event.getClass())) {
                applicableListeners.add(listener);
            }
        }
        if(applicableListeners.isEmpty()) {
            if (eventStackTraceEnabled.contains(event.getClass())) {
                logger.info(
                        "No applicable listeners found for event: {}.",
                        event.getClass().getSimpleName());
            }
            return event;
        }
        if (eventStackTraceEnabled.contains(event.getClass())) {
            logger.info("Dispatching event: {} to {} listeners", event.getClass().getSimpleName(), applicableListeners.size());
        }
        for (ListenerData applicableListener : applicableListeners) {
            applicableListener.getGameListener().onEvent(event);
        }
        return event;
    }

    private void eventHandler(@NotNull Object event) {
        Game<?, ?, ?> foundGame = getGameFromEvent(event);
        if (foundGame == null) return;

        try {
            callEvent(event, foundGame);
        } catch (EventDispatchException e) {
            logger.error(
                    "Error occurred while calling {} event listener",
                    event.getClass().getSimpleName(),
                    e);
        }
    }

    public @Nullable Game<?, ?, ?> getGameFromEvent(@NotNull Object event) {
        Class<?> eventType = event.getClass();
        @NotNull Collection<EventGameAdapter> adapters = findAdapters(event);

        if (adapters.isEmpty()) {
            if (!noAdapters.contains(eventType)) {
                noAdapters.add(eventType);
                logger.info("No event adapter found for event type: {}", eventType);
            }
            return null;
        }

        Game<?, ?, ?> foundGame = null;
        for (EventGameAdapter adapter : adapters) {
            try {
                foundGame = adapter.convert(event);
            } catch (EventAdaptException e) {
                logger.error("Error occurred while converting event", e);
            }
            if (foundGame != null) break;
        }
        return foundGame;
    }

    public void registerListener(@NotNull Game<?, ?, ?> game, @NotNull ListenerData listener) throws EventDispatchException {
        if (!registeredTypes.contains(listener.getType())) {
            registeredTypes.add(listener.getType());
            logger.info("Registering listener for event type: {}", listener.getType());
            gameEventPlatform.registerEventListener(game, listener.getType(), this::eventHandler);
        }

        List<ListenerData> listeners = gameListeners.computeIfAbsent(game.getId(), k -> new ArrayList<>());
        listeners.add(listener);
        gameListeners.put(game.getId(), listeners);
    }

    public void unregisterListener(@NotNull Game<?, ?, ?> game, @NotNull Class<?> eventClass, @NotNull GameListener listener) {
        List<ListenerData> listeners = gameListeners.get(game.getId());
        if (listeners == null) return;

        listeners.removeIf(listenerData -> listenerData.getGameListener() == listener);
    }

    public void registerAdapter(@NotNull Class<?> eventType, @NotNull GameEventAdapter adapter) {
        eventAdapters.put(eventType, adapter);
    }

    private @NotNull Collection<EventGameAdapter> findAdapters(@NotNull Object event) {
        List<EventGameAdapter> result = new ArrayList<>();
        Class<?> eventClass = event.getClass();
        for (Map.Entry<Class<?>, EventGameAdapter> entry : eventAdapters.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventClass)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public void enableEventStackTrace(Class<?> clazz) {
        eventStackTraceEnabled.add(clazz);
    }

    public void shutdown(@NotNull Game<?, ?, ?> game) {
        gameEventPlatform.unregisterListeners(game);
        gameListeners.remove(game.getId());
    }

    public @NotNull GameEventPlatform getPlatform() {
        return gameEventPlatform;
    }
}