package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.ScheduledTask;
import com.l1sk1sh.vladikbot.models.FixedScheduledExecutor;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.StringUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Oliver Johnson
 */
public class AutoTextBackupDaemon implements ScheduledTask {
    private static final Logger log = LoggerFactory.getLogger(AutoTextBackupDaemon.class);
    private final FixedScheduledExecutor fixedScheduledExecutor;
    private final Bot bot;

    public AutoTextBackupDaemon(Bot bot) {
        this.bot = bot;
        fixedScheduledExecutor = new FixedScheduledExecutor(this, bot.getThreadPool());
    }

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

        List<TextChannel> availableChannels = bot.getAvailableTextChannels();

        new Thread(() -> {
            bot.setLockedAutoBackup(true);
            log.info("Automatic text backup has started it's execution.");

            for (TextChannel channel : availableChannels) {
                log.info("Starting auto text backup of channel '{}' at guild '{}'.", channel.getName(), channel.getGuild());

                try {
                    String pathToBackup = bot.getBotSettings().getRotationBackupFolder() + "text/"
                            + channel.getGuild().getName() + "/" + StringUtils.getCurrentDate() + "/";
                    FileUtils.createFolders(pathToBackup);

                    /* Creating new thread from text backup service and waiting for it to finish */
                    BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                            bot,
                            channel.getId(),
                            Const.BackupFileType.PLAIN_TEXT,
                            pathToBackup,
                            null,
                            null,
                            false
                    );

                    Thread backupChannelServiceThread = new Thread(backupTextChannelService);
                    backupChannelServiceThread.start();
                    try {
                        backupChannelServiceThread.join();
                    } catch (InterruptedException e) {
                        bot.getNotificationService().sendEmbeddedError(channel.getGuild(), "Text backup process was interrupted!");
                        return;
                    }

                    if (backupTextChannelService.hasFailed()) {
                        bot.getNotificationService().sendEmbeddedError(channel.getGuild(),
                                String.format("Text channel backup has failed: `[%1$s]`", backupTextChannelService.getFailMessage()));
                        return;
                    }

                    log.info("Finished auto text backup of '{}'.", channel.getName());

                } catch (Exception e) {
                    log.error("Failed to create auto backup:", e);
                    bot.getNotificationService().sendEmbeddedError(channel.getGuild(),
                            String.format("Auto text rotation of chat `%1$s` has failed due to: `%2$s`!", channel.getName(), e.getLocalizedMessage()));
                } finally {
                    bot.setLockedAutoBackup(false);
                }
            }
            log.info("Automatic text backup has finished it's execution.");
        }).start();
    }

    public void start() {
        int dayDelay = bot.getBotSettings().getDelayDaysForAutoTextBackup();
        int targetHour = bot.getBotSettings().getTargetHourForAutoTextBackup();
        int targetMin = 0;
        int targetSec = 0;
        fixedScheduledExecutor.startExecutionAt(dayDelay, targetHour, targetMin, targetSec);
        log.info(String.format("Text backup will be performed in %2d days at %02d:%02d:%02d local time.", dayDelay, targetHour, targetMin, targetSec));
    }

    public void stop() {
        log.info("Cancelling scheduled auto text task...");
        fixedScheduledExecutor.stop();
    }
}
