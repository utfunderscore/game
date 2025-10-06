package org.readutf.engine.minestom.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.GameManager;
import org.readutf.engine.event.GameEventPlatform;
import org.readutf.engine.event.adapter.EventGameAdapter;
import org.readutf.engine.event.defaults.ServerJoinListener;
import org.readutf.engine.event.defaults.ServerLeaveListener;
import org.readutf.engine.event.exceptions.EventDispatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinestomEventPlatform implements GameEventPlatform {

    private static final Logger log = LoggerFactory.getLogger(MinestomEventPlatform.class);
    private final Map<UUID, EventNode<Event>> listeners = new HashMap<>();

    @Override
    public <T> void registerEventListener(
            @NotNull Game<?, ?, ?> game, @NotNull Class<T> type, @NotNull Consumer<T> consumer) {
        if (Event.class.isAssignableFrom(type)) {
            Class<? extends Event> eventType = type.asSubclass(Event.class);

            @NotNull EventNode<Event> eventNode = listeners.computeIfAbsent(game.getId(), uuid -> MinecraftServer.getGlobalEventHandler().addChild(EventNode.all("game-events-" + game.getId())));

            eventNode.addListener(eventType, event -> {
                try {
                    consumer.accept(type.cast(event));
                } catch (Exception e) {
                    log.error("Error handling event {}", event, e);
                }
            });

        }
    }

    @Override
    public void unregisterListeners(@NotNull Game<?, ?, ?> game) {
        EventNode<Event> node = listeners.get(game.getId());
        MinecraftServer.getGlobalEventHandler().removeChild(node);
    }

    @Override
    public void registerAdapters(Map<Class<?>, EventGameAdapter> eventAdapters) {
        eventAdapters.put(InstanceEvent.class, new InstanceEventAdapter(GameManager.getInstance()));
        eventAdapters.put(EntityEvent.class, new EntityEventAdapter(GameManager.getInstance()));
    }

    @Override
    public void registerServerJoinListener(ServerJoinListener serverJoinListener) {
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e -> {
            if (!e.isFirstSpawn()) return;

            serverJoinListener.onJoin(e.getPlayer().getUuid());
        });
    }

    @Override
    public void registerServerLeaveListener(ServerLeaveListener serverLeaveListener) {

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e ->
                serverLeaveListener.onServerLeave(e.getPlayer().getUuid())
        );
    }
}
