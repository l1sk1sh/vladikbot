package com.multiheaded.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.services.ClearTmpService;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class ClearTmpCommand extends OwnerCommand {
    private final VladikBot bot;

    public ClearTmpCommand(VladikBot bot) {
        this.name = "cleartmp";
        this.help = "completely clears tmp folder of the bot";
        this.guildOnly = false;
        this.ownerCommand = true;
        this.bot = bot;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.replyWarning("Clearing tmp folder, my master!");

        try {
            if (bot.isBackupAvailable()) {
                new ClearTmpService(bot.getSettings().getLocalPathToExport(), bot::setAvailableBackup).clear();
            } else {
                event.replyWarning("Can't clear tmp folder, as it is being used by other process!");
            }
        } catch (IOException ioe) {
            event.replyError(String.format("Something went wrong during clearing of tmp folder! `[%s]`",
                    ioe.getLocalizedMessage()));
        } catch (NullPointerException npe) {
            event.replyWarning("Nothing to clear - the directory is empty.");
        }
    }
}
