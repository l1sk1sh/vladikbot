package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.ClearTmpService;
import com.l1sk1sh.vladikbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class ClearTmpCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(ClearTmpCommand.class);
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
            event.replyWarning("Clearing temp folder...");

            new ClearTmpService(bot).clear();
            log.info("'{}' folder was cleared.", bot.getBotSettings().getLocalTmpFolder());
        } catch (IOException ioe) {
            log.error("Failed to clear '{}' folder.", bot.getBotSettings().getLocalTmpFolder(), ioe);
            event.replyError(String.format("Something went wrong during clearing of tmp folder! `[%1$s][%2$s]`",
                    ioe.getClass().getSimpleName(), ioe.getLocalizedMessage()));
        }
    }
}
