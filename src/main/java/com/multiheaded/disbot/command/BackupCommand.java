package com.multiheaded.disbot.command;

import com.multiheaded.disbot.process.BackupProcess;
import com.multiheaded.disbot.process.CleanProcess;
import com.multiheaded.disbot.process.CopyProcess;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.multiheaded.disbot.DisBot.settings;
import static com.multiheaded.disbot.settings.Constants.BOT_PREFIX;

public class BackupCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(BackupCommand.class);

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        if (!e.isFromType(ChannelType.PRIVATE)) {
            sendMessage(e, "Initializing backup process. Be patient...");

            new Thread(() -> {
                CopyProcess cp = null;

                try {
                    BackupProcess bp = new BackupProcess(settings.token, e.getChannel().getId(), settings.dockerContainerName);
                    bp.getThread().join();

                    if(bp.isCompleted()) {
                        cp = new CopyProcess(settings.dockerContainerName,
                                settings.dockerPathToExport, settings.localPathToExport);
                        cp.getThread().join();
                    }

                } catch (Exception error) {
                    logger.error("Backup thread interrupted on command level.", error.getCause());
                } finally {
                    new CleanProcess(settings.dockerContainerName);
                }

                if (Objects.requireNonNull(cp).isCompleted()) {
                    sendMessage(e, "**Backup is complete.** File is available in the local storage of the bot.");
                } else {
                    sendMessage(e, "Oooops! **Backup failed!** Read logs.");
                }
            }).start();

        }
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(BOT_PREFIX + "backup", BOT_PREFIX + "backup_chats");
    }

    @Override
    public String getDescription() {
        return "Backup chat history to bot's server local storage.";
    }

    @Override
    public String getName() {
        return "Backup Chat Command";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
                BOT_PREFIX + "backup - creates *local* backup of the current channel.");
    }
}
