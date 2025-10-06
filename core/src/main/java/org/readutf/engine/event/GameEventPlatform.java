package org.readutf.engine.event;

import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.readutf.engine.Game;
import org.readutf.engine.event.adapter.EventGameAdapter;
import org.readutf.engine.event.defaults.ServerLeaveListener;
import org.readutf.engine.event.exceptions.EventDispatchException;
import org.readutf.engine.event.defaults.ServerJoinListener;

public interface GameEventPlatform {

    <T> void registerEventListener(@NotNull Game<?, ?, ?> game, @NotNull Class<T> type, @NotNull Consumer<T> consumer) throws EventDispatchException;

    void unregisterListeners(@NotNull Game<?, ?, ?> game);

    void registerAdapters(Map<Class<?>, EventGameAdapter> eventAdapters);

    void registerServerJoinListener(ServerJoinListener serverJoinListener);

    void registerServerLeaveListener(ServerLeaveListener serverLeaveListener);

}
