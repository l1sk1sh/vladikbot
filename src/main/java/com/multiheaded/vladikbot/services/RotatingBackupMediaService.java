package com.multiheaded.vladikbot.services;

import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.models.RotatingTask;
import com.multiheaded.vladikbot.models.RotatingTaskExecutor;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.utils.FileUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

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
        if (bot.getBotSettings().shouldRotateMediaBackup()) {
            List<TextChannel> availableChannels = bot.getAvailableTextChannels();

            new Thread(() -> {
                for (TextChannel channel : availableChannels) {
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Starting media backup of channel %s", channel.getName()));
                    try {
                        String pathToBackup = bot.getBotSettings().getLocalPathToExport() + "/backup/media/"
                                + channel.getGuild().getId() + "/";
                        FileUtils.createFolders(pathToBackup);

                        new BackupMediaService(
                                new BackupChannelService(
                                        channel.getId(),
                                        bot.getBotSettings().getToken(),
                                        Constants.BACKUP_PLAIN_TEXT,
                                        bot.getBotSettings().getLocalPathToExport(),
                                        bot.getBotSettings().getDockerPathToExport(),
                                        bot.getBotSettings().getDockerContainerName(),
                                        new String[]{"-f"},
                                        bot::setAvailableBackup
                                ).getExportedFile(),
                                channel.getId(),
                                pathToBackup,
                                pathToBackup,
                                String.format("%s - %s [%s] - media list",
                                        channel.getGuild().getName(),
                                        channel.getName(),
                                        channel.getId()),
                                new String[]{"-f"},
                                bot::setAvailableBackup
                        );
                        bot.getNotificationService().sendMessage(channel.getGuild(),
                                "Automatic media rotation backup has finished.");
                    } catch (Exception e) {
                        logger.error("Failed to create rotation backup: {}", e);
                        bot.getNotificationService().sendMessage(channel.getGuild(),
                                String.format("Automatic media rotation backup has failed due to: %s",
                                        e.getLocalizedMessage()));
                    }
                }
            }).start();
        }
    }

    public void enableExecution() {
        int targetHour = bot.getBotSettings().getTargetHourForBackup();
        int targetMin = 0;
        int targetSec = 0;
        rotatingTaskExecutor.startExecutionAt(targetHour, targetMin, targetSec);
        logger.info(String.format("Media backup will be performed at %s:%s:%s local time", targetHour, targetMin, targetSec));
    }

    public void disableExecution() throws InterruptedException {
        rotatingTaskExecutor.stop();
    }
}
