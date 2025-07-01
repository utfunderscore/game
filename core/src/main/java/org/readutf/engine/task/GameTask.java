package org.readutf.engine.task;

import org.slf4j.Logger;

public abstract class GameTask {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GameTask.class);

    protected long startTime = System.currentTimeMillis();

    private boolean markedForRemoval = false;

    public abstract void tick();

    public void markForRemoval() {
        markedForRemoval = true;
    }

    public void cancel() {
        markForRemoval();
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
