package com.l1sk1sh.vladikbot.models;

/**
 * @author l1sk1sh
 */
public interface ScheduledTask {
    String getTaskName();

    void execute();

    @SuppressWarnings("unused")
    void start();

    @SuppressWarnings("unused")
    void stop();
}
