package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Added permissions
 * @author John Grosh
 */
public class ShutdownCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(ShutdownCommand.class);
    private final Bot bot;

    public ShutdownCommand(Bot bot) {
        this.bot = bot;
        this.name = "shutdown";
        this.help = "safely shuts down";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected final void execute(CommandEvent event) {
        log.info("Bot is being shutdown by {}:[{}]", event.getAuthor().getName(), event.getAuthor().getId());
        event.replyWarning("Shutting down...");
        bot.shutdown();
    }
}
