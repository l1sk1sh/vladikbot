package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.FixedScheduledExecutor;
import com.l1sk1sh.vladikbot.models.ScheduledTask;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.DateAndTimeUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.StringUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class AutoTextBackupDaemon implements ScheduledTask {
    private static final Logger log = LoggerFactory.getLogger(AutoTextBackupDaemon.class);
    private final FixedScheduledExecutor fixedScheduledExecutor;
    private final Bot bot;
    private final static int MAX_AMOUNT_OF_BACKUPS_PER_GUILD = 3;
    private final static int MIN_DAY_BEFORE_BACKUP = 1;

    public AutoTextBackupDaemon(Bot bot) {
        this.bot = bot;
        this.fixedScheduledExecutor = new FixedScheduledExecutor(this, bot.getBackThreadPool());
    }

    @Override
    public String getTaskName() {
        return AutoTextBackupDaemon.class.getSimpleName();
    }

    @Override
    public void execute() {
        if (!bot.isDockerRunning()) {
            return;
        }

        if (!bot.getBotSettings().shouldAutoTextBackup()) {
            return;
        }

        if (bot.isLockedAutoBackup()) {
            return;
        }

        if (bot.isLockedBackup()) {
            /* pool-4-thread-1 is trying to call "execute" multiple times */
            return;
        }
        bot.setLockedAutoBackup(true);

        bot.getOfflineStorage().setLastAutoTextBackupTime(System.currentTimeMillis());

        List<TextChannel> availableChannels = bot.getAvailableTextChannels();
        List<String> failedTextChannels = new ArrayList<>();

        log.info("Automatic text backup has started it's execution.");

        for (TextChannel channel : availableChannels) {
            log.info("Starting auto text backup of channel '{}' at guild '{}'.", channel.getName(), channel.getGuild());

            try {
                String pathToGuildBackup = bot.getBotSettings().getRotationBackupFolder() + "text/"
                        + channel.getGuild().getId() + "/";

                String pathToDateBackup = pathToGuildBackup
                        + StringUtils.getNormalizedCurrentDate() + "/";

                FileUtils.createFolderIfAbsent(pathToDateBackup);

                /* Creating new thread from text backup service and waiting for it to finish */
                BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                        bot,
                        channel.getId(),
                        Const.BackupFileType.PLAIN_TEXT,
                        pathToDateBackup,
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

                File[] directories = new File(pathToGuildBackup).listFiles(File::isDirectory);
                if (directories != null && directories.length > MAX_AMOUNT_OF_BACKUPS_PER_GUILD) {
                    log.debug("Auto text backup reached limit of allowed backups. Clearing...");

                    File oldestDirectory = null;
                    long oldestDate = Long.MAX_VALUE;

                    for (File directory : directories) {
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

            } catch (Exception e) {
                log.error("Failed to create auto backup:", e);
                bot.getNotificationService().sendEmbeddedError(channel.getGuild(),
                        String.format("Auto text backup of chat `%1$s` has failed due to: `%2$s`!", channel.getName(), e.getLocalizedMessage()));
                failedTextChannels.add(channel.getName());

            } finally {
                bot.setLockedAutoBackup(false);
            }
        }

        log.info("Automatic text backup has finished it's execution.");
        bot.getNotificationService().sendEmbeddedInfo(null, String.format("Auto text backup has finished. %1$s",
                (failedTextChannels.isEmpty())
                        ? "All channels were backed up."
                        : "Failed channels: `" + Arrays.toString(failedTextChannels.toArray()) + "`")
        );
    }

    @Override
    public void start() {
        long lastBackupTime = (bot.getOfflineStorage().getLastAutoTextBackupTime() == 0)
                ? System.currentTimeMillis()
                : bot.getOfflineStorage().getLastAutoTextBackupTime();
        int differenceInDays = DateAndTimeUtils.getDifferenceInDaysBetweenUnixTimestamps(lastBackupTime, System.currentTimeMillis());

        int dayDelay = (differenceInDays >= bot.getBotSettings().getDelayDaysForAutoTextBackup())
                ? bot.getBotSettings().getDelayDaysForAutoTextBackup()
                : MIN_DAY_BEFORE_BACKUP;
        int targetHour = bot.getBotSettings().getTargetHourForAutoTextBackup();
        int targetMin = 0;
        int targetSec = 0;
        fixedScheduledExecutor.startExecutionAt(dayDelay, bot.getBotSettings().getDelayDaysForAutoTextBackup(), targetHour, targetMin, targetSec);
        log.info(String.format("Text backup will be performed in %2d days at %02d:%02d:%02d local time. " +
                "Consequent tasks will be launched with fixed delay in %2d days.",
                dayDelay, targetHour, targetMin, targetSec, bot.getBotSettings().getDelayDaysForAutoTextBackup()));
    }

    @Override
    public void stop() {
        log.info("Cancelling scheduled auto text backup task...");
        fixedScheduledExecutor.stop();
    }
}
