package org.readutf.engine.task


interface GameSchedulerPlatform {

    /**
     * Schedules a task. The implementation defines how this is actually run.
     *
     * @param runnable the task to execute
     */
    fun scheduleTask(runnable: Runnable)
}