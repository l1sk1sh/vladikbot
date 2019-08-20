package com.multiheaded.vladikbot.services;

import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.models.RotatingTask;
import com.multiheaded.vladikbot.models.RotatingTaskExecutor;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.utils.FileUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oliver Johnson
 */
public class RotatingBackupChannelService implements RotatingTask {
    private static final Logger logger = LoggerFactory.getLogger(BackupChannelService.class);
    private RotatingTaskExecutor rotatingTaskExecutor;
    private Bot bot;

    public RotatingBackupChannelService(Bot bot) {
        this.bot = bot;
        rotatingTaskExecutor = new RotatingTaskExecutor(this);
    }

    public void execute() {
        if (bot.getBotSettings().shouldRotateTextBackup()) {
            User selfUser = bot.getJDA().getSelfUser();

            List<TextChannel> allChannels = bot.getJDA().getGuilds().stream()
                    .map(Guild::getTextChannels).flatMap(Collection::stream).collect(Collectors.toList());

            List<TextChannel> availableChannels = allChannels.stream().filter(textChannel ->
                    textChannel.getMembers().stream().anyMatch(
                            member -> member.getUser().getAsTag().equals(selfUser.getAsTag())
                    )
            ).collect(Collectors.toList());

            new Thread(() -> {
                for (TextChannel channel : availableChannels) {
                    try {
                        String pathToBackup = bot.getBotSettings().getLocalPathToExport() + "/backup/text/"
                                + channel.getGuild().getId() + "/";
                        FileUtils.createFolders(pathToBackup);

                        new BackupChannelService(
                                channel.getId(),
                                bot.getBotSettings().getToken(),
                                Constants.BACKUP_HTML_DARK,
                                pathToBackup,
                                bot.getBotSettings().getDockerPathToExport(),
                                bot.getBotSettings().getDockerContainerName(),
                                new String[]{"-f"},
                                bot::setAvailableBackup
                        );
                    } catch (Exception e) {
                        logger.error("Failed to create rotation backup: {}", e);
                        bot.getNotificationService().sendMessage(channel.getGuild(),
                                String.format("Automatic rotation backup has failed due to: %s", e.getLocalizedMessage()));
                    }
                }
            }).start();
        }
    }

    public void enableExecution() {
        rotatingTaskExecutor.startExecutionAt(bot.getBotSettings().getTargetHourForBackup(), 0, 0);
    }

    public void disableExecution() throws InterruptedException {
        rotatingTaskExecutor.stop();
    }
}
