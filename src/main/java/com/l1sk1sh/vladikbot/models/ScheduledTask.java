package com.l1sk1sh.vladikbot.models;

/**
 * @author Oliver Johnson
 */
public interface ScheduledTask {
    void execute();
    void start();
    void stop();
}
