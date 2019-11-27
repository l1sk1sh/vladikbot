package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.ClearTmpService;
import com.l1sk1sh.vladikbot.Bot;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class ClearTmpCommand extends OwnerCommand {
    private final Bot bot;

    public ClearTmpCommand(Bot bot) {
        this.bot = bot;
        this.name = "cleartmp";
        this.help = "completely clears tmp folder of the bot";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            if (bot.isLockedBackup()) {
                event.replyWarning("Can't clear tmp folder, as it is being used by other process!");
                return;
            }
            event.replyWarning("Clearing tmp folder, my master!");

            new ClearTmpService(bot.getBotSettings().getLocalTmpPath(), bot::setLockedBackup).clear();

        } catch (IOException ioe) {
            event.replyError(String.format("Something went wrong during clearing of tmp folder! `[%s][%s]`",
                    ioe.getClass().getSimpleName(), ioe.getLocalizedMessage()));
        }
    }
}
