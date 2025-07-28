package org.readutf.engine.task;

import org.slf4j.Logger;

public abstract class GameTask {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GameTask.class);

    private final String taskName;

    public GameTask(String taskName) {
        this.taskName = taskName;
    }

    public GameTask() {
        this.taskName = getClass().getName() + '@' + Integer.toHexString(hashCode());
    }

    protected long startTime = System.currentTimeMillis();
    protected boolean markedForRemoval = false;

    public abstract void tick();

    public void markForRemoval() {
        LOGGER.info("Marking task {} for removal", taskName);
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
        return "GameTask{" +
                "taskName='" + taskName + '\'' +
                ", startTime=" + startTime +
                ", markedForRemoval=" + markedForRemoval +
                '}';
    }
}
