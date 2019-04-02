package com.multiheaded.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.conductors.services.ClearTmpService;
import com.multiheaded.vladikbot.settings.SettingsManager;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class ClearTmpCommand extends OwnerCommand {

    public ClearTmpCommand() {
        this.name = "cleartmp";
        this.help = "completely clears tmp folder of the bot";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.replyWarning("Clearing tmp folder, my master!");

        try {
            new ClearTmpService(SettingsManager.getInstance().getSettings().getLocalPathToExport()).clear();
        } catch (IOException ioe) {
            event.replyError(String.format("Something went wrong during clearing of tmp folder! `[%s]`",
                    ioe.getMessage()));
        } catch (NullPointerException npe) {
            event.replyWarning("Nothing to clear - the directory is empty.");
        }
    }
}
