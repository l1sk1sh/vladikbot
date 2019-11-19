package com.multiheaded.vladikbot.services;

import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.models.RotatingTask;
import com.multiheaded.vladikbot.models.RotatingTaskExecutor;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.utils.FileUtils;
import com.multiheaded.vladikbot.utils.StringUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class RotatingBackupMediaService implements RotatingTask {
    private static final Logger logger = LoggerFactory.getLogger(RotatingBackupMediaService.class);
    private RotatingTaskExecutor rotatingTaskExecutor;
    private Bot bot;

    public RotatingBackupMediaService(Bot bot) {
        this.bot = bot;
        rotatingTaskExecutor = new RotatingTaskExecutor(this);
    }

    public void execute() {
        if (!bot.getBotSettings().shouldRotateMediaBackup()) {
            return;
        }

        if (!bot.isAvailableRotationBackup()) {
            /* pool-4-thread-1 is trying to call "execute" multiple times */
            return;
        }

        List<TextChannel> availableChannels = bot.getAvailableTextChannels();

        new Thread(() -> {
            bot.setAvailableRotationBackup(false);

            for (TextChannel channel : availableChannels) {
                logger.info("Starting text backup of {}", channel.getName());
                bot.getNotificationService().sendMessage(channel.getGuild(),
                        String.format("Starting media backup of channel %s", channel.getName()));
                try {
                    String pathToBackup = bot.getBotSettings().getLocalPathToExport() + "/backup/media/"
                            + channel.getGuild().getName() + "/" + StringUtils.getCurrentDate() + "/";
                    FileUtils.createFolders(pathToBackup);

                    File exportedFile = new BackupChannelService(
                            channel.getId(),
                            bot.getBotSettings().getToken(),
                            Constants.BACKUP_PLAIN_TEXT,
                            bot.getBotSettings().getLocalPathToExport(),
                            bot.getBotSettings().getDockerPathToExport(),
                            bot.getBotSettings().getDockerContainerName(),
                            new String[]{"-f"},
                            bot::setAvailableBackup
                    ).getExportedFile();

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
                            bot::setAvailableBackup
                    );

                    logger.info("Finished text backup of {}", channel.getName());
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Automatic media rotation backup of chat `%s` has finished.", channel.getName()));
                } catch (Exception e) {
                    logger.error("Failed to create rotation backup", e);
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Automatic media rotation backup has failed due to: %s", e.getLocalizedMessage()));
                    break;
                } finally {
                    bot.setAvailableRotationBackup(true);
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
        logger.info(String.format("Media backup will be performed at %s:%s:%s local time", targetHour, targetMin, targetSec));
    }

    public void disableExecution() throws InterruptedException {
        rotatingTaskExecutor.stop();
    }
}
