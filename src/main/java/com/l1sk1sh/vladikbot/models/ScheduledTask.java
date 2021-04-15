package com.l1sk1sh.vladikbot.models;

/**
 * @author Oliver Johnson
 */
public interface ScheduledTask {
    String getTaskName();

    void execute();

    @SuppressWarnings("unused")
    void start();

    @SuppressWarnings("unused")
    void stop();
}
