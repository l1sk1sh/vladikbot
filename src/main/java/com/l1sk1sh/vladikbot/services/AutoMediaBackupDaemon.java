package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.RotatingTask;
import com.l1sk1sh.vladikbot.models.RotatingTaskExecutor;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.StringUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class AutoMediaBackupDaemon implements RotatingTask {
    private static final Logger log = LoggerFactory.getLogger(AutoMediaBackupDaemon.class);
    private final RotatingTaskExecutor rotatingTaskExecutor;
    private final Bot bot;

    public AutoMediaBackupDaemon(Bot bot) {
        this.bot = bot;
        this.rotatingTaskExecutor = new RotatingTaskExecutor(this);
    }

    public void execute() {
        if (bot.isDockerFailed()) {
            return;
        }

        if (!bot.getBotSettings().shouldRotateMediaBackup()) {
            return;
        }

        if (bot.isLockedAutoBackup()) {
            log.debug("Rotation media backup is already running. Exiting...");
            return;
        }

        if (bot.isLockedBackup()) {
            /* pool-4-thread-1 is trying to call "execute" multiple times */
            return;
        }

        List<TextChannel> availableChannels = bot.getAvailableTextChannels();

        new Thread(() -> {
            bot.setLockedAutoBackup(true);

            for (TextChannel channel : availableChannels) {
                log.info("Starting text backup for media rotation backup of channel {} at guild {}", channel.getName(), channel.getGuild());

                try {
                    String pathToBackup = bot.getBotSettings().getRotationBackupFolder() + "media/"
                            + channel.getGuild().getName() + "/" + StringUtils.getCurrentDate() + "/";
                    FileUtils.createFolders(pathToBackup);

                    /* Creating new thread from text backup service and waiting for it to finish */
                    BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                            bot,
                            channel.getId(),
                            Const.BackupFileType.HTML_DARK,
                            bot.getBotSettings().getLocalTmpFolder(),
                            new String[]{}
                    );

                    Thread backupChannelServiceThread = new Thread(backupTextChannelService);
                    backupChannelServiceThread.start();
                    try {
                        backupChannelServiceThread.join();
                    } catch (InterruptedException e) {
                        bot.getNotificationService().sendMessage(channel.getGuild(), "Text backup process required for media backup was interrupted!");
                        return;
                    }

                    if (backupTextChannelService.hasFailed()) {
                        bot.getNotificationService().sendMessage(channel.getGuild(),
                                String.format("Text channel backup required for media backup has failed: `[%1$s]`", backupTextChannelService.getFailMessage()));
                        return;
                    }

                    File exportedTextFile = backupTextChannelService.getBackupFile();

                    BackupMediaService backupMediaService = new BackupMediaService(
                            bot,
                            channel.getId(),
                            exportedTextFile,
                            pathToBackup,
                            new String[]{"--zip"}
                    );

                    /* Creating new thread from media backup service and waiting for it to finish */
                    Thread backupMediaServiceThread = new Thread(backupMediaService);
                    log.info("Starting backupMediaService...");
                    backupMediaServiceThread.start();
                    try {
                        backupMediaServiceThread.join();
                    } catch (InterruptedException e) {
                        bot.getNotificationService().sendMessage(channel.getGuild(), "Media backup process was interrupted!");
                        return;
                    }

                    if (backupMediaService.hasFailed()) {
                        log.error("BackupMediaService has failed: {}", backupTextChannelService.getFailMessage());
                        bot.getNotificationService().sendMessage(channel.getGuild(),String.format("Media backup has filed: `[%1$s]`", backupMediaService.getFailMessage()));
                        return;
                    }

                    log.info("Finished rotation media backup of {}", channel.getName());

                } catch (Exception e) {
                    log.error("Failed to create rotation backup", e);
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Media rotation backup of chat `%1$s` has failed due to: `%2$s`", channel.getName(), e.getLocalizedMessage()));
                    break;
                } finally {
                    bot.setLockedAutoBackup(false);
                }
            }
        }).start();
    }

    public void enableExecution() {
        int dayDelay = bot.getBotSettings().getDelayDaysForMediaBackup();
        int targetHour = bot.getBotSettings().getTargetHourForMediaBackup();
        int targetMin = 0;
        int targetSec = 0;
        rotatingTaskExecutor.startExecutionAt(dayDelay, targetHour, targetMin, targetSec);
        log.info(String.format("Media backup will be performed in %2d days at %02d:%02d:%02d local time", dayDelay, targetHour, targetMin, targetSec));
    }

    public void disableExecution() throws InterruptedException {
        rotatingTaskExecutor.stop();
    }
}
