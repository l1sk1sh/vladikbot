package com.multiheaded.disbot.process;

public abstract class AbstractProcess {
    ProcessBuilder pb;
    Thread thread;
    boolean running = false;
    boolean completed = false;

    public synchronized boolean isCompleted() {
        return completed;
    }

    public synchronized Thread getThread() {
        return thread;
    }
}
