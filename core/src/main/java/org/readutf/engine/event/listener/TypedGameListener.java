package org.readutf.engine.event.listener;

import org.jetbrains.annotations.NotNull;

public interface TypedGameListener<T> extends GameListener {

    void onTypedEvent(@NotNull T event);

    @Override
    @SuppressWarnings("UNCHECKED_CAST")
    default void accept(@NotNull Object event) {
        onTypedEvent((T) event);
    }
}
