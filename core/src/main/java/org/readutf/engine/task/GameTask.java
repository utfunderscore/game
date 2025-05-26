package org.readutf.engine.task;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GameTask {

    protected long startTime = System.currentTimeMillis();

    @Getter private boolean markedForRemoval = false;

    public abstract void tick();

    public void markForRemoval() {
        markedForRemoval = true;
    }

    public void cancel() {
        markForRemoval();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
