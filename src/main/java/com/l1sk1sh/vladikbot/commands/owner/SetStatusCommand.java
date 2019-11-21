package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.OnlineStatus;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetStatusCommand extends OwnerCommand {
    public SetStatusCommand() {
        this.name = "setstatus";
        this.help = "sets the status the bot displays";
        this.arguments = "<status>";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            OnlineStatus status = OnlineStatus.fromKey(event.getArgs());
            if (status == OnlineStatus.UNKNOWN) {
                event.replyError("Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`");
            } else {
                event.getJDA().getPresence().setStatus(status);
                event.replySuccess(String.format("Set the status to `%1$s`", status.getKey().toUpperCase()));
            }
        } catch (Exception e) {
            event.replyError("The status could not be set!");
        }
    }
}
