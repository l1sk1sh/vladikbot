package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.models.FixedScheduledExecutor;
import com.l1sk1sh.vladikbot.models.ScheduledTask;
import com.l1sk1sh.vladikbot.services.notification.ChatNotificationService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import com.l1sk1sh.vladikbot.utils.DateAndTimeUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 */
@Service
public class AutoTextBackupDaemon implements ScheduledTask {
    private static final Logger log = LoggerFactory.getLogger(AutoTextBackupDaemon.class);

    private static final int MAX_AMOUNT_OF_BACKUPS_PER_CHANNEL = 2;
    private static final int MIN_DAY_BEFORE_BACKUP = 1;

    private final BotSettingsManager settings;
    private final DockerService dockerService;
    private final ChatNotificationService notificationService;
    private final FixedScheduledExecutor fixedScheduledExecutor;

    @Autowired
    public AutoTextBackupDaemon(@Qualifier("backgroundThreadPool") ScheduledExecutorService backgroundThreadPool,
                                BotSettingsManager settings, DockerService dockerService, ChatNotificationService notificationService) {
        this.settings = settings;
        this.dockerService = dockerService;
        this.notificationService = notificationService;
        this.fixedScheduledExecutor = new FixedScheduledExecutor(this, backgroundThreadPool);
    }

    @Override
    public String getTaskName() {
        return AutoTextBackupDaemon.class.getSimpleName();
    }

    @Override
    public void execute() {
        if (!settings.get().isDockerRunning()) {
            return;
        }

        if (!settings.get().isAutoTextBackup()) {
            return;
        }

        if (settings.get().isLockedAutoBackup()) {
            return;
        }

        if (settings.get().isLockedBackup()) {
            /* pool-4-thread-1 is trying to call "execute" multiple times */
            return;
        }
        settings.get().setLockedAutoBackup(true);

        settings.get().setLastAutoTextBackupTime(System.currentTimeMillis());

        List<TextChannel> availableChannels = BotUtils.getAvailableTextChannels(VladikBot.jda());
        List<String> failedTextChannels = new ArrayList<>();

        log.info("Automatic text backup has started it's execution.");

        for (TextChannel channel : availableChannels) {
            log.info("Starting auto text backup of channel '{}' at guild '{}'.", channel.getName(), channel.getGuild());

            try {
                String pathToGuildBackup = settings.get().getRotationBackupFolder() + "text/"
                        + channel.getGuild().getId() + "/";

                FileUtils.createFolderIfAbsent(pathToGuildBackup);

                /* Creating new thread from text backup service and waiting for it to finish */
                BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                        settings,
                        dockerService, channel.getId(),
                        Const.BackupFileType.PLAIN_TEXT,
                        pathToGuildBackup,
                        null,
                        null,
                        false
                );

                Thread backupChannelServiceThread = new Thread(backupTextChannelService);
                backupChannelServiceThread.start();
                try {
                    backupChannelServiceThread.join();
                } catch (InterruptedException e) {
                    log.error("Text backup process for channel '{}' was interrupted.", channel.getName());
                    failedTextChannels.add(channel.getName());
                    continue;
                }

                if (backupTextChannelService.hasFailed()) {
                    log.error("Text channel backup has failed: [{}]", backupTextChannelService.getFailMessage());
                    failedTextChannels.add(channel.getName());
                    continue;
                }

                log.info("Finished auto text backup of '{}'.", channel.getName());

                deleteOldBackups(pathToGuildBackup);

            } catch (Exception e) {
                log.error("Failed to create auto backup:", e);
                notificationService.sendEmbeddedError(channel.getGuild(),
                        String.format("Auto text backup of chat `%1$s` has failed due to: `%2$s`!", channel.getName(), e.getLocalizedMessage()));
                failedTextChannels.add(channel.getName());

            } finally {
                settings.get().setLockedAutoBackup(false);
            }
        }

        log.info("Automatic text backup has finished it's execution.");
        notificationService.sendEmbeddedInfo(null, String.format("Auto text backup has finished. %1$s",
                (failedTextChannels.isEmpty())
                        ? "All channels were backed up."
                        : "Failed channels: `" + Arrays.toString(failedTextChannels.toArray()) + "`")
        );
    }

    private void deleteOldBackups(String pathToGuildBackup) throws IOException {
        File[] channelsDirectories = new File(pathToGuildBackup).listFiles(File::isDirectory);

        for (File channelDirectory : channelsDirectories) {
            File[] timeBackupDirectories = new File(channelDirectory.getAbsolutePath()).listFiles(File::isDirectory);

            if (timeBackupDirectories != null && timeBackupDirectories.length > MAX_AMOUNT_OF_BACKUPS_PER_CHANNEL) {
                log.debug("Auto text backup reached limit of allowed backups for '{}'. Clearing...", channelDirectory.getParent());

                File oldestDirectory = null;
                long oldestDate = Long.MAX_VALUE;

                for (File directory : timeBackupDirectories) {
                    if (directory.lastModified() < oldestDate) {
                        oldestDate = directory.lastModified();
                        oldestDirectory = directory;
                    }
                }

                if (oldestDirectory != null) {
                    org.apache.commons.io.FileUtils.deleteDirectory(oldestDirectory);
                    log.info("Directory '{}' has been removed.", oldestDirectory.getPath());
                }
            }
        }
    }

    @Override
    public void start() {
        log.info("Enabling auto text backup service...");

        long lastBackupTime = (settings.get().getLastAutoTextBackupTime() == 0)
                ? System.currentTimeMillis()
                : settings.get().getLastAutoTextBackupTime();
        int differenceInDays = DateAndTimeUtils.getDifferenceInDaysBetweenUnixTimestamps(lastBackupTime, System.currentTimeMillis());

        int dayDelay = (differenceInDays >= settings.get().getDelayDaysForAutoTextBackup())
                ? settings.get().getDelayDaysForAutoTextBackup()
                : MIN_DAY_BEFORE_BACKUP;
        int targetHour = settings.get().getTargetHourForAutoTextBackup();
        int targetMin = 0;
        int targetSec = 0;
        fixedScheduledExecutor.startExecutionAt(dayDelay, settings.get().getDelayDaysForAutoTextBackup(), targetHour, targetMin, targetSec);
        log.info(String.format("Text backup will be performed in %2d days at %02d:%02d:%02d local time. " +
                        "Consequent tasks will be launched with fixed delay in %2d days.",
                dayDelay, targetHour, targetMin, targetSec, settings.get().getDelayDaysForAutoTextBackup()));
    }

    @Override
    public void stop() {
        log.info("Cancelling scheduled auto text backup task...");
        fixedScheduledExecutor.stop();
    }
}
