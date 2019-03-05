package com.multiheaded.disbot.command;

import com.multiheaded.disbot.service.BackupService;
import com.multiheaded.disbot.util.FileUtils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.multiheaded.disbot.settings.Constants.BOT_PREFIX;
import static com.multiheaded.disbot.DisBot.settings;

public class BackupCommand extends AbstractCommand {

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        if (!e.isFromType(ChannelType.PRIVATE)) {
            sendMessage(e, "Initializing backup process. Be patient...");

            new Thread(() -> {
                BackupService bs = new BackupService(e.getChannel().getId(), "HtmlDark");

                if (bs.isCompleted()) {
                    String pathToFile = settings.localPathToExport + settings.dockerPathToExport;
                    String exportedFile = FileUtils.getFileNameByIdAndExtension(
                            pathToFile,
                            e.getChannel().getId(),
                            ".html");
                    File exportedHtml = new File(Objects.requireNonNull(exportedFile));

                    e.getTextChannel().sendFile(exportedHtml,
                            e.getChannel().getName() + ".html").queue();
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
        return "Backup chat history and sends it to the chat.";
    }

    @Override
    public String getName() {
        return "Backup Chat Command";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
                BOT_PREFIX + "backup - creates backup of the current channel.");
    }
}
