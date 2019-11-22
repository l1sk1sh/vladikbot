package com.l1sk1sh.vladikbot.utils;

import java.util.Timer;
import java.util.TimerTask;

public final class SystemUtils {
    private SystemUtils() {}

    /**
     * Exits the JVM, trying to do it nicely, otherwise doing it nastily.
     * Additional info: https://blog.joda.org/2014/02/exiting-jvm.html
     *
     * @param status  the exit status, zero for OK, non-zero for error
     * @param maxDelayMillis  the maximum delay in milliseconds
     */
    public static void exit(final int status, long maxDelayMillis) {
        try {
            /* Setup a timer, so if nice exit fails, the nasty exit happens */
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Runtime.getRuntime().halt(status);
                }
            }, maxDelayMillis);
            /* Try to exit nicely */
            System.exit(status);

        } catch (Throwable ex) {
            /* Exit nastily if we have a problem */
            Runtime.getRuntime().halt(status);
        } finally {
            /* Should never get here */
            Runtime.getRuntime().halt(status);
        }
    }
}
