package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.RotatingTask;
import com.l1sk1sh.vladikbot.models.RotatingTaskExecutor;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.StringUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Oliver Johnson
 */
public class RotatingMediaBackupDaemon implements RotatingTask {
    private static final Logger log = LoggerFactory.getLogger(RotatingMediaBackupDaemon.class);
    private final RotatingTaskExecutor rotatingTaskExecutor;
    private final Bot bot;

    public RotatingMediaBackupDaemon(Bot bot) {
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

        if (bot.isLockedBackup() || bot.isLockedRotationBackup()) {
            /* pool-4-thread-1 is trying to call "execute" multiple times */
            return;
        }

        List<TextChannel> availableChannels = bot.getAvailableTextChannels();

        new Thread(() -> {
            bot.setLockedRotationBackup(true);

            for (TextChannel channel : availableChannels) {
                log.info("Starting text backup of {}", channel.getName());
                bot.getNotificationService().sendMessage(channel.getGuild(),
                        String.format("Starting media backup of channel %1$s", channel.getName()));

                try {
                    String pathToBackup = bot.getBotSettings().getRotationBackupFolder() + "/media/"
                            + channel.getGuild().getName() + "/" + StringUtils.getCurrentDate() + "/";
                    FileUtils.createFolders(pathToBackup);

                    //TODO Fix rotation

                    /*File exportedFile = new BackupTextChannelService(
                            channel.getId(),
                            bot.getBotSettings().getToken(),
                            Const.BACKUP_PLAIN_TEXT,
                            bot.getBotSettings().getLocalPathToExport(),
                            bot.getBotSettings().getDockerPathToExport(),
                            bot.getBotSettings().getDockerContainerName(),
                            new String[]{"-f"},
                            bot::setLockedBackup
                    ).getBackupFile();

                    new BackupMediaService(
                            exportedFile,
                            channel.getId(),
                            pathToBackup,
                            pathToBackup,
                            String.format("%s - %s [%s] - media list",
                                    channel.getGuild().getName(),
                                    channel.getName(),
                                    channel.getId()),
                            new String[]{"-f", "-z"},
                            bot::setLockedBackup
                    );*/

                    log.info("Finished text backup of {}", channel.getName());
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Media rotation backup of chat `%1$s` has finished.", channel.getName()));
                } catch (Exception e) {
                    log.error("Failed to create rotation backup", e);
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Media rotation backup of chat `%1$s` has failed due to: `%2$s`",
                                    channel.getName(), e.getLocalizedMessage()));
                    break;
                } finally {
                    bot.setLockedRotationBackup(false);
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
