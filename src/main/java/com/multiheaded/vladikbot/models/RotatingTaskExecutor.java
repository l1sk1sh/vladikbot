package com.multiheaded.vladikbot.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 */
public class RotatingTaskExecutor {
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private RotatingTask task;

    public RotatingTaskExecutor(RotatingTask task) {
        this.task = task;
    }

    public void startExecutionAt(int targetHour, int targetMin, int targetSec) {
        Runnable taskWrapper = () -> {
            task.execute();
            startExecutionAt(targetHour, targetMin, targetSec);
        };
        long delay = computeNextDelay(targetHour, targetMin, targetSec);
        executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
    }

    private long computeNextDelay(int targetHour, int targetMin, int targetSec) {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNextTarget = zonedNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
        if (zonedNow.compareTo(zonedNextTarget) > 0)
            zonedNextTarget = zonedNextTarget.plusDays(1);

        Duration duration = Duration.between(zonedNow, zonedNextTarget);
        return duration.getSeconds();
    }

    public void stop() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.DAYS);
    }
}
