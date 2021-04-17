package com.l1sk1sh.vladikbot.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author l1sk1sh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemUtils {

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

    public static void exit(final int status) {
        long defaultMaxDelayMillis = 5000;
        exit(status, defaultMaxDelayMillis);
    }

    /* This method compromises security of JVM instantiation. Might be used only in case when -D options cannot be set */
    @SuppressWarnings("unused")
    public static void setRuntimeEncoding() throws IllegalAccessException, NoSuchFieldException {
        System.setProperty("file.encoding", "UTF-8");
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, null);
    }

    public static void resetLoggerContext() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextInitializer ci = new ContextInitializer(lc);
        lc.reset();
        try {

            /* Prefer autoConfig() over JoranConfigurator.doConfigure() so there is no need to find the file manually */
            ci.autoConfig();
        } catch (JoranException e) {

            /* StatusPrinter will try to log this */
            e.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }
}
