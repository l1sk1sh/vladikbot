package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Added permissions
 * @author John Grosh
 */
public class ShutdownCommand extends OwnerCommand {
    private final Bot bot;

    public ShutdownCommand(Bot bot) {
        this.bot = bot;
        this.name = "shutdown";
        this.help = "safely shuts down";
        this.guildOnly = false;
        this.ownerCommand = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.replyWarning("Shutting down...");
        bot.shutdown();
    }
}
