package com.multiheaded.disbot.command;

import com.multiheaded.disbot.core.BackupConductor;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.multiheaded.disbot.settings.Constants.BOT_PREFIX;

public class BackupCommand extends AbstractCommand {

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {

        if (!event.isFromType(ChannelType.PRIVATE)) {
            sendMessage(event, "Initializing backup process. Be patient...");

            new Thread(() -> {
                try {
                    BackupConductor backupConductor = new BackupConductor(event.getChannel().getId(), args);

                    event.getTextChannel().sendFile(backupConductor.getBackupService().getExportedFile(),
                            backupConductor.getBackupService().getExportedFile().getName()).queue();
                } catch (InterruptedException ie) {
                    sendMessage(event, String.format("Backup **has failed**! [%s]", ie.getMessage()));
                } catch (InvalidParameterException ipe) {
                    sendMessage(event, ipe.getMessage());
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
                BOT_PREFIX + "backup **OR** " + BOT_PREFIX + "backup *<arguments>*\n"
                        + BOT_PREFIX + "backup - creates backup of the current channel.\n"
                        + BOT_PREFIX + "backup <arguments> - creates backup of the current channel within specified period.\n"
                        + "\t\t -b, --before <mm/dd/yyyy> - specifies date till which backup would be done.\n"
                        + "\t\t -a, --after  <mm/dd/yyyy> - specifies date from which backup would be done.");
    }
}
