package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.models.FixedScheduledExecutor;
import com.l1sk1sh.vladikbot.models.ScheduledTask;
import com.l1sk1sh.vladikbot.services.notification.ChatNotificationService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import com.l1sk1sh.vladikbot.utils.DateAndTimeUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Oliver Johnson
 */
@Service
public class AutoMediaBackupDaemon implements ScheduledTask {
    private static final Logger log = LoggerFactory.getLogger(AutoMediaBackupDaemon.class);

    private final static int MIN_DAY_BEFORE_BACKUP = 1;

    private final JDA jda;
    private final BotSettingsManager settings;
    private final DockerService dockerService;
    private final ChatNotificationService notificationService;
    private final FixedScheduledExecutor fixedScheduledExecutor;

    @Autowired
    public AutoMediaBackupDaemon(JDA jda, @Qualifier("backgroundThreadPool") ScheduledExecutorService backgroundThreadPool,
                                 BotSettingsManager settings, DockerService dockerService, ChatNotificationService notificationService) {
        this.jda = jda;
        this.settings = settings;
        this.dockerService = dockerService;
        this.notificationService = notificationService;
        this.fixedScheduledExecutor = new FixedScheduledExecutor(this, backgroundThreadPool);
    }

    @Override
    public String getTaskName() {
        return AutoMediaBackupDaemon.class.getSimpleName();
    }

    @Override
    public void execute() {
        if (!settings.get().isDockerRunning()) {
            return;
        }

        if (!settings.get().isAutoMediaBackup()) {
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

        settings.get().setLastAutoMediaBackupTime(System.currentTimeMillis());

        List<TextChannel> availableChannels = BotUtils.getAvailableTextChannels(jda);
        List<String> failedMediaChannels = new ArrayList<>();

        log.info("Automatic media backup has started it's execution.");

        for (TextChannel channel : availableChannels) {
            log.info("Starting text backup for auto media backup of channel {} at guild {}", channel.getName(), channel.getGuild());

            try {
                String pathToGuildBackup = settings.get().getRotationBackupFolder() + "media/"
                        + channel.getGuild().getId() + "/";

                FileUtils.createFolderIfAbsent(pathToGuildBackup);

                /* Creating new thread from text backup service and waiting for it to finish */
                BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                        settings,
                        dockerService, channel.getId(),
                        Const.BackupFileType.HTML_DARK,
                        settings.get().getLocalTmpFolder(),
                        null,
                        null,
                        false
                );

                Thread backupChannelServiceThread = new Thread(backupTextChannelService);
                backupChannelServiceThread.start();
                try {
                    backupChannelServiceThread.join();
                } catch (InterruptedException e) {
                    notificationService.sendEmbeddedError(channel.getGuild(), "Text backup process required for media backup was interrupted!");
                    continue;
                }

                if (backupTextChannelService.hasFailed()) {
                    log.error("Text channel backup required for media backup has failed: [{}]", backupTextChannelService.getFailMessage());
                    failedMediaChannels.add(channel.getName());
                    continue;
                }

                File exportedTextFile = backupTextChannelService.getBackupFile();

                BackupMediaService backupMediaService = new BackupMediaService(
                        settings,
                        channel.getId(),
                        exportedTextFile,
                        pathToGuildBackup,
                        new String[]{"--download"}
                );

                /* Creating new thread from media backup service and waiting for it to finish */
                Thread backupMediaServiceThread = new Thread(backupMediaService);
                log.info("Starting backupMediaService...");
                backupMediaServiceThread.start();
                try {
                    backupMediaServiceThread.join();
                } catch (InterruptedException e) {
                    log.error("Media backup process for channel '{}' was interrupted.", channel.getName());
                    failedMediaChannels.add(channel.getName());
                    continue;
                }

                if (backupMediaService.hasFailed()) {
                    log.error("BackupMediaService for channel '{}' has failed: {}", channel.getName(), backupTextChannelService.getFailMessage());
                    failedMediaChannels.add(channel.getName());
                    continue;
                }

                log.info("Finished auto media backup of {}", channel.getName());

            } catch (Exception e) {
                log.error("Failed to create auto media backup", e);
                notificationService.sendEmbeddedError(channel.getGuild(),
                        String.format("Auto media backup of chat `%1$s` has failed due to: `%2$s`", channel.getName(), e.getLocalizedMessage()));
                failedMediaChannels.add(channel.getName());

            } finally {
                settings.get().setLockedAutoBackup(false);
            }
        }

        log.info("Automatic media backup has finished it's execution.");
        notificationService.sendEmbeddedInfo(null, String.format("Auto media backup has finished. %1$s",
                (failedMediaChannels.isEmpty())
                        ? "All channels were backed up."
                        : "Failed channels: `" + Arrays.toString(failedMediaChannels.toArray()) + "`")
        );
    }

    @Override
    public void start() {
        log.info("Enabling auto media backup service...");

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
}
