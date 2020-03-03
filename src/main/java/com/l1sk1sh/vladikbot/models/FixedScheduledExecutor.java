package com.l1sk1sh.vladikbot.models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 */
public class FixedScheduledExecutor {
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledFuture;
    private final ScheduledTask task;

    public FixedScheduledExecutor(ScheduledTask task, ScheduledExecutorService executorService) {
        this.task = task;
        this.executorService = executorService;
    }

    public void startExecutionAt(int dayDelay, int targetHour, int targetMin, int targetSec) {
        Runnable taskWrapper = () -> {
            task.execute();
            startExecutionAt(dayDelay, targetHour, targetMin, targetSec);
        };
        long delay = computeNextDelay(dayDelay, targetHour, targetMin, targetSec);
        scheduledFuture = executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
    }

    private long computeNextDelay(int dayDelay,int targetHour, int targetMin, int targetSec) {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNextTarget = zonedNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
        if (zonedNow.compareTo(zonedNextTarget) > 0) {
            zonedNextTarget = zonedNextTarget.plusDays(dayDelay);
        }

        Duration duration = Duration.between(zonedNow, zonedNextTarget);
        return duration.getSeconds();
    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }
}
