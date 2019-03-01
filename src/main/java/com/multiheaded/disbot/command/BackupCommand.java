package com.multiheaded.disbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.multiheaded.disbot.settings.Constants.BOT_PREFIX;

public class BackupCommand extends AbstractCommand {

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {

    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(BOT_PREFIX + "backup", BOT_PREFIX + "backup_chats");
    }

    @Override
    public String getDescription() {
        return "Backup chat history to local storage of the BOT.";
    }

    @Override
    public String getName() {
        return "Backup Chats Command";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
                BOT_PREFIX + "backup  **OR**  " + BOT_PREFIX + "backup *<command>*\n"
                + BOT_PREFIX + "backup - returns the list of commands along with a simple description of each.\n"
                + BOT_PREFIX + "backup <command> - returns \n");
    }
}
