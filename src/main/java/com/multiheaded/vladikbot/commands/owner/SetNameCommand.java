package com.multiheaded.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetNameCommand extends OwnerCommand {
    public SetNameCommand() {
        this.name = "setname";
        this.help = "sets the name of the bot";
        this.arguments = "<name>";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String oldname = event.getSelfUser().getName();
            event.getSelfUser().getManager().setName(event.getArgs()).complete(false);
            event.replySuccess(String.format("Name changed from `%1$s` to `%2$s`.", oldname, event.getArgs()));
        } catch (RateLimitedException e) {
            event.replyError("Name can only be changed twice per hour!");
        } catch (Exception e) {
            event.replyError("That name is not valid!");
        }
    }
}
