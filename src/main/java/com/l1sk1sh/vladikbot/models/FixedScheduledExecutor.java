package com.l1sk1sh.vladikbot.models;

import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author l1sk1sh
 */
@Slf4j
public class FixedScheduledExecutor {

    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledFuture;
    private final ScheduledTask task;

    public FixedScheduledExecutor(ScheduledTask task, ScheduledExecutorService executorService) {
        this.task = task;
        this.executorService = executorService;
    }

    /**
     * This command starts scheduled execution of the same task, with possible custom time for the first launch
     *
     * @param dayDelayFirst set this delay for external calls, for initial scheduling of the task. Later or,
     *                      it will be set to -1, that will ensure usage of second argument
     * @param dayDelayUsual usual day delay that start working after initial execution (with custom delay)
     * @param targetHour    target local zone hour to execute task
     * @param targetMin     target local zone minute to execute task
     * @param targetSec     target local zone second to execute task
     */
    public void startExecutionAt(int dayDelayFirst, int dayDelayUsual, int targetHour, int targetMin, int targetSec) {
        int dayDelay = (dayDelayFirst != -1) ? dayDelayFirst : dayDelayUsual;

        Runnable taskWrapper = () -> {
            task.execute();
            startExecutionAt(-1, dayDelayUsual, targetHour, targetMin, targetSec);
        };

        long delay = computeNextDelay(dayDelay, targetHour, targetMin, targetSec);
        scheduledFuture = executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
        log.info("Added task to thread pool {} that will launch at {}", task.getTaskName(),
                FormatUtils.getDateAndTimeFromDatetime(new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delay))));
    }

    private long computeNextDelay(int dayDelay, int targetHour, int targetMin, int targetSec) {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNextTarget = zonedNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
        if (zonedNow.compareTo(zonedNextTarget) >= 0) {
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
