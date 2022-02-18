package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.models.FixedScheduledExecutor;
import com.l1sk1sh.vladikbot.models.ScheduledTask;
import com.l1sk1sh.vladikbot.services.notification.ChatNotificationService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.DateAndTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class AutoMediaBackupDaemon implements ScheduledTask, OnBackupCompletedListener {

    private static final int MIN_DAY_BEFORE_BACKUP = 1;

    private final BotSettingsManager settings;
    private final ChatNotificationService notificationService;
    private final FixedScheduledExecutor fixedScheduledExecutor;
    private final BackupMediaService backupMediaService;

    @Autowired
    public AutoMediaBackupDaemon(@Qualifier("backgroundThreadPool") ScheduledExecutorService backgroundThreadPool,
                                 BotSettingsManager settings, ChatNotificationService notificationService,
                                 BackupMediaService backupMediaService) {
        this.settings = settings;
        this.notificationService = notificationService;
        this.fixedScheduledExecutor = new FixedScheduledExecutor(this, backgroundThreadPool);
        this.backupMediaService = backupMediaService;
    }

    @Override
    public String getTaskName() {
        return AutoMediaBackupDaemon.class.getSimpleName();
    }

    @Override
    public void execute() {
        if (!settings.get().isAutoMediaBackup()) {
            return;
        }

        if (settings.get().isLockedAutoBackup()) {
            /* pool-4-thread-1 is trying to call "execute" multiple times */
            return;
        }
        settings.get().setLockedAutoBackup(true);
        settings.get().setLastAutoMediaBackupTime(System.currentTimeMillis());
        log.info("Automatic media backup has started it's execution.");
        backupMediaService.downloadAllAttachments(this);
    }

    @Override
    public void start() {
        log.info("Enabling auto media backup service...");

        if (!settings.get().isAutoTextBackup()) {
            log.warn("Auto media backup will not perform as expected with disabled auto text backup!");
        }

        long lastBackupTime = (settings.get().getLastAutoMediaBackupTime() == 0)
                ? System.currentTimeMillis()
                : settings.get().getLastAutoMediaBackupTime();
        int differenceInDays = DateAndTimeUtils.getDifferenceInDaysBetweenUnixTimestamps(lastBackupTime, System.currentTimeMillis());

        int dayDelay = (differenceInDays >= settings.get().getDelayDaysForAutoMediaBackup())
                ? settings.get().getDelayDaysForAutoMediaBackup()
                : MIN_DAY_BEFORE_BACKUP;
        int targetHour = settings.get().getTargetHourForAutoMediaBackup();
        int targetMin = 0;
        int targetSec = 0;
        fixedScheduledExecutor.startExecutionAt(dayDelay, settings.get().getDelayDaysForAutoMediaBackup(), targetHour, targetMin, targetSec);
        log.info(String.format("Media backup will be performed in %2d days at %02d:%02d:%02d local time. " +
                        "Consequent tasks will be launched with fixed delay in %2d days.",
                dayDelay, targetHour, targetMin, targetSec, settings.get().getDelayDaysForAutoMediaBackup()));
    }

    @Override
    public void stop() {
        log.info("Cancelling scheduled auto media task...");
        fixedScheduledExecutor.stop();
    }

    @Override
    public void onBackupCompleted(boolean success, String message) {
        log.info("Automatic media backup has finished it's execution with success - {} ({}).", success, message);
        settings.get().setLockedAutoBackup(false);
        if (success) {
            notificationService.sendEmbeddedInfo(null, "Auto media backup has finished.");
        } else {
            notificationService.sendEmbeddedError(null, String.format("Auto media backup has failed. %1$s", message));
        }
    }
}
