package com.l1sk1sh.vladikbot.services.backup;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.ScheduledTask;
import com.l1sk1sh.vladikbot.models.FixedScheduledExecutor;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import com.l1sk1sh.vladikbot.utils.DateAndTimeUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.StringUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class AutoMediaBackupDaemon implements ScheduledTask {
    private static final Logger log = LoggerFactory.getLogger(AutoMediaBackupDaemon.class);
    private final FixedScheduledExecutor fixedScheduledExecutor;
    private final Bot bot;
    private final static int MAX_AMOUNT_OF_BACKUPS_PER_GUILD = 2;
    private final static int MIN_DAY_BEFORE_BACKUP = 1;

    public AutoMediaBackupDaemon(Bot bot) {
        this.bot = bot;
        this.fixedScheduledExecutor = new FixedScheduledExecutor(this, bot.getThreadPool());
    }

    public void execute() {
        if (!bot.isDockerRunning()) {
            return;
        }

        if (!bot.getBotSettings().shouldAutoMediaBackup()) {
            return;
        }

        if (bot.isLockedAutoBackup()) {
            return;
        }

        if (bot.isLockedBackup()) {
            /* pool-4-thread-1 is trying to call "execute" multiple times */
            return;
        }

        bot.getOfflineStorage().setLastAutoMediaBackupTime(System.currentTimeMillis());

        List<TextChannel> availableChannels = bot.getAvailableTextChannels();

        new Thread(() -> {
            bot.setLockedAutoBackup(true);
            log.info("Automatic media backup has started it's execution.");

            List<String> failedMediaChannels = new ArrayList<>();

            for (TextChannel channel : availableChannels) {
                log.info("Starting text backup for auto media backup of channel {} at guild {}", channel.getName(), channel.getGuild());

                try {
                    String pathToGuildBackup = bot.getBotSettings().getRotationBackupFolder() + "media/"
                            + BotUtils.getNormalizedGuildName(channel.getGuild()) + "/";

                    String pathToDateBackup = pathToGuildBackup
                            + StringUtils.getNormalizedCurrentDate() + "/";

                    FileUtils.createFolderIfAbsent(pathToDateBackup);

                    /* Creating new thread from text backup service and waiting for it to finish */
                    BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                            bot,
                            channel.getId(),
                            Const.BackupFileType.HTML_DARK,
                            bot.getBotSettings().getLocalTmpFolder(),
                            null,
                            null,
                            false
                    );

                    Thread backupChannelServiceThread = new Thread(backupTextChannelService);
                    backupChannelServiceThread.start();
                    try {
                        backupChannelServiceThread.join();
                    } catch (InterruptedException e) {
                        bot.getNotificationService().sendEmbeddedError(channel.getGuild(), "Text backup process required for media backup was interrupted!");
                        return;
                    }

                    if (backupTextChannelService.hasFailed()) {
                        bot.getNotificationService().sendEmbeddedError(channel.getGuild(),
                                String.format("Text channel backup required for media backup has failed: `[%1$s]`", backupTextChannelService.getFailMessage()));
                        return;
                    }

                    File exportedTextFile = backupTextChannelService.getBackupFile();

                    BackupMediaService backupMediaService = new BackupMediaService(
                            bot,
                            channel.getId(),
                            exportedTextFile,
                            pathToDateBackup,
                            new String[]{"--zip"}
                    );

                    /* Creating new thread from media backup service and waiting for it to finish */
                    Thread backupMediaServiceThread = new Thread(backupMediaService);
                    log.info("Starting backupMediaService...");
                    backupMediaServiceThread.start();
                    try {
                        backupMediaServiceThread.join();
                    } catch (InterruptedException e) {
                        bot.getNotificationService().sendEmbeddedError(channel.getGuild(), "Media backup process was interrupted!");
                        return;
                    }

                    if (backupMediaService.hasFailed()) {
                        log.error("BackupMediaService has failed: {}", backupTextChannelService.getFailMessage());
                        bot.getNotificationService().sendEmbeddedError(channel.getGuild(), String.format("Media backup has filed: `[%1$s]`", backupMediaService.getFailMessage()));
                        return;
                    }

                    log.info("Finished auto media backup of {}", channel.getName());

                    File[] directories = new File(pathToGuildBackup).listFiles(File::isDirectory);
                    if (directories != null && directories.length > MAX_AMOUNT_OF_BACKUPS_PER_GUILD) {
                        log.debug("Auto media backup reached limit of allowed backups. Clearing...");

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
                    log.error("Failed to create auto media backup", e);
                    bot.getNotificationService().sendEmbeddedError(channel.getGuild(),
                            String.format("Auto media backup of chat `%1$s` has failed due to: `%2$s`", channel.getName(), e.getLocalizedMessage()));
                    failedMediaChannels.add(channel.getName());

                } finally {
                    bot.setLockedAutoBackup(false);
                }
            }

            log.info("Automatic media backup has finished it's execution.");
            bot.getNotificationService().sendEmbeddedInfo(null, String.format("Auto media backup has finished. `%1$s`",
                    (failedMediaChannels.isEmpty())
                            ? "All channels were backed up."
                            : "Failed channels: " + Arrays.toString(failedMediaChannels.toArray()))
            );

        }).start();
    }

    public void start() {
        long lastBackupTime = (bot.getOfflineStorage().getLastAutoMediaBackupTime() == 0)
                ? System.currentTimeMillis()
                : bot.getOfflineStorage().getLastAutoMediaBackupTime();
        int differenceInDays = DateAndTimeUtils.getDifferenceInDaysBetweenUnixTimestamps(lastBackupTime, System.currentTimeMillis());

        int dayDelay = (differenceInDays >= bot.getBotSettings().getDelayDaysForAutoMediaBackup())
                ? bot.getBotSettings().getDelayDaysForAutoMediaBackup()
                : MIN_DAY_BEFORE_BACKUP;
        int targetHour = bot.getBotSettings().getTargetHourForAutoMediaBackup();
        int targetMin = 0;
        int targetSec = 0;
        fixedScheduledExecutor.startExecutionAt(dayDelay, targetHour, targetMin, targetSec);
        log.info(String.format("Media backup will be performed in %2d days at %02d:%02d:%02d local time", dayDelay, targetHour, targetMin, targetSec));
    }

    public void stop() {
        log.info("Cancelling scheduled auto media task...");
        fixedScheduledExecutor.stop();
    }
}
