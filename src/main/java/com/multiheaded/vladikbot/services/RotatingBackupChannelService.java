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

import java.util.List;

/**
 * @author Oliver Johnson
 */
public class RotatingBackupChannelService implements RotatingTask {
    private static final Logger logger = LoggerFactory.getLogger(RotatingBackupChannelService.class);
    private RotatingTaskExecutor rotatingTaskExecutor;
    private Bot bot;

    public RotatingBackupChannelService(Bot bot) {
        this.bot = bot;
        rotatingTaskExecutor = new RotatingTaskExecutor(this);
    }

    public void execute() {
        if (!bot.getBotSettings().shouldRotateTextBackup()) {
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
                        String.format("Starting text backup of channel `%s`", channel.getName()));
                try {
                    String pathToBackup = bot.getBotSettings().getLocalPathToExport() + "/backup/text/"
                            + channel.getGuild().getName() + "/" + StringUtils.getCurrentDate() + "/";
                    FileUtils.createFolders(pathToBackup);

                    new BackupChannelService(
                            channel.getId(),
                            bot.getBotSettings().getToken(),
                            Constants.BACKUP_PLAIN_TEXT,
                            pathToBackup,
                            bot.getBotSettings().getDockerPathToExport(),
                            bot.getBotSettings().getDockerContainerName(),
                            new String[]{"-f"},
                            bot::setAvailableBackup
                    );

                    logger.info("Finished text backup of {}", channel.getName());
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Automatic text rotation backup of chat `%s` has finished.", channel.getName()));
                } catch (Exception e) {
                    logger.error("Failed to create rotation backup", e);
                    bot.getNotificationService().sendMessage(channel.getGuild(),
                            String.format("Automatic text rotation backup has failed due to: %s", e.getLocalizedMessage()));
                    break;
                } finally {
                    bot.setAvailableRotationBackup(true);
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
        logger.info(String.format("Text backup will be performed at %s:%s:%s local time", targetHour, targetMin, targetSec));
    }

    public void disableExecution() throws InterruptedException {
        rotatingTaskExecutor.stop();
    }
}
