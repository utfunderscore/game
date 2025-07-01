package org.readutf.engine.feature.visibility;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.impl.game.GameJoinEvent;
import org.readutf.engine.event.listener.ListenerData;
import org.readutf.engine.event.listener.TypedGameListener;
import org.readutf.engine.feature.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VisibilitySystem implements System {

    private static final Logger logger = LoggerFactory.getLogger(VisibilitySystem.class);

    private @NotNull final Game<?, ?, ?> game;
    private @NotNull final List<VisibilityHandler> visibilityLayers;
    private @NotNull final VisibilityPlatform platform;
    private @NotNull final List<Class<?>> updateTriggers;
    private @NotNull final TypedGameListener<Object> eventListener;
    private @NotNull final TypedGameListener<GameJoinEvent> gameJoinListener;
    private @NotNull final TypedGameListener<GameJoinEvent> gameLeaveListener;

    public VisibilitySystem(@NotNull Game<?, ?, ?> game, @NotNull VisibilityPlatform platform) {
        this.game = game;
        this.platform = platform;
        this.visibilityLayers = new ArrayList<>();
        this.updateTriggers = new ArrayList<>();
        this.eventListener = event -> {
            if (updateTriggers.contains(event.getClass())) {
                logger.info(
                        "Refreshing visibility for all online players due to event: {}",
                        event.getClass().getSimpleName());
                for (UUID onlinePlayer : game.getOnlinePlayers()) {
                    platform.setPlayerVisibility(onlinePlayer, getCombinedVisibilityHandler());
                }
            }
        };
        this.gameJoinListener = event -> {
            UUID playerId = event.getPlayerId();
            platform.setPlayerVisibility(playerId, getCombinedVisibilityHandler());
        };
        this.gameLeaveListener = event -> {
            UUID playerId = event.getPlayerId();
            platform.setPlayerVisibility(playerId, (viewer, target) -> true);
        };
    }

    public void addVisibilityLayer(VisibilityHandler handler) {
        visibilityLayers.add(handler);
    }

    public void removeVisibilityLayer(VisibilityHandler handler) {
        visibilityLayers.remove(handler);
    }

    public void addUpdateTrigger(Class<?> trigger) {
        updateTriggers.add(trigger);
    }

    public void refresh() {
        for (UUID onlinePlayer : game.getOnlinePlayers()) {
            platform.refreshVisibility(onlinePlayer);
        }
    }

    @Override
    public @NotNull List<ListenerData> getListeners() {
        return List.of(
                ListenerData.typed(GameJoinEvent.class, gameJoinListener),
                ListenerData.typed(GameJoinEvent.class, gameLeaveListener),
                ListenerData.typed(Object.class, eventListener));
    }

    private VisibilityHandler getCombinedVisibilityHandler() {
        return (viewer, target) -> {
            for (VisibilityHandler handler : visibilityLayers) {
                if (!handler.isVisibleToPlayer(viewer, target)) {
                    logger.info("{} cannot see {}", viewer, target);
                    return false;
                }
            }
            logger.info("{} can see {}", viewer, target);
            return true;
        };
    }
}
