package com.multiheaded.disbot.core.service.process;

public abstract class AbstractProcess {
    ProcessBuilder pb;
    Thread thread;
    boolean running = false;
    boolean failed = true;

    public synchronized boolean isFailed() {
        return failed;
    }

    public synchronized Thread getThread() {
        return thread;
    }
}
