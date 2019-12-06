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

import java.util.List;

/**
 * @author Oliver Johnson
 */
public class RotatingTextBackupDaemon implements RotatingTask {
    private static final Logger log = LoggerFactory.getLogger(RotatingTextBackupDaemon.class);
    private final RotatingTaskExecutor rotatingTaskExecutor;
    private final Bot bot;

    public RotatingTextBackupDaemon(Bot bot) {
        this.bot = bot;
        rotatingTaskExecutor = new RotatingTaskExecutor(this);
    }

    public void execute() {
        if (bot.isDockerFailed()) {
            return;
        }

        if (!bot.getBotSettings().shouldRotateTextBackup()) {
            return;
        }

        if (bot.isLockedBackup() || bot.isLockedRotationBackup()) {
            /* pool-4-thread-1 is trying to call "execute" multiple times */
            return;
        }

        List<TextChannel> availableChannels = bot.getAvailableTextChannels();

        new Thread(() -> {
            bot.setLockedRotationBackup(true);

            for (TextChannel channel : availableChannels) {
                log.info("Starting text backup of {}", channel.getName());

                try {
                    String pathToBackup = bot.getBotSettings().getRotationBackupFolder() + "/text/"
                            + channel.getGuild().getName() + "/" + StringUtils.getCurrentDate() + "/";
                    FileUtils.createFolders(pathToBackup);

                    BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                            bot,
                            channel.getId(),
                            Const.BACKUP_PLAIN_TEXT,
                            pathToBackup,
                            new String[]{}
                    );

                    Thread backupChannelServiceThread = new Thread(backupTextChannelService);
                    backupChannelServiceThread.start();
                    try {
                        backupChannelServiceThread.join();
                    } catch (InterruptedException e) {
                        bot.getNotificationService().sendMessage(channel.getGuild(), "Text backup process was interrupted!");
                        return;
                    }

                    if (backupTextChannelService.hasFailed()) {
                        bot.getNotificationService().sendMessage(channel.getGuild(),
                                String.format("Text channel backup has failed: `[%1$s]`", backupTextChannelService.getFailMessage()));
                        return;
                    }

                    log.info("Finished text backup of {}", channel.getName());
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Text rotation backup of chat `%1$s` has finished.", channel.getName()));
                } catch (Exception e) {
                    log.error("Failed to create rotation backup", e);
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Text rotation backup of chat `%1$s` has failed due to: `%2$s`", channel.getName(), e.getLocalizedMessage()));
                } finally {
                    bot.setLockedRotationBackup(false);
                }
            }
        }).start();
    }

    public void enableExecution() {
        int dayDelay = bot.getBotSettings().getDelayDaysForTextBackup();
        int targetHour = bot.getBotSettings().getTargetHourForTextBackup();
        int targetMin = 0;
        int targetSec = 0;
        rotatingTaskExecutor.startExecutionAt(dayDelay, targetHour, targetMin, targetSec);
        log.info(String.format("Text backup will be performed in %2d days at %02d:%02d:%02d local time", dayDelay, targetHour, targetMin, targetSec));
    }

    public void disableExecution() throws InterruptedException {
        rotatingTaskExecutor.stop();
    }
}
