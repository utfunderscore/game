package org.readutf.engine.feature;

import org.jetbrains.annotations.NotNull;
import org.readutf.engine.event.listener.GameListener;
import org.readutf.engine.task.GameTask;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class Feature {

    /**
     * Returns a map of event types to their corresponding listeners.
     * Subclasses can override to provide actual event listeners.
     */
    public @NotNull Map<Class<?>, GameListener> getListeners() {
        return Collections.emptyMap();
    }

    /**
     * Returns a list of tasks to be run for this feature.
     * Subclasses can override to provide scheduled tasks.
     */
    public @NotNull List<GameTask> getTasks() {
        return Collections.emptyList();
    }

    /**
     * Shutdown logic to be implemented by subclasses.
     */
    public abstract void shutdown();
}
