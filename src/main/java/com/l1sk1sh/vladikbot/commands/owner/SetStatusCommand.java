package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.OnlineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class SetStatusCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(SetStatusCommand.class);

    @Autowired
    public SetStatusCommand() {
        this.name = "setstatus";
        this.help = "sets the status the bot displays";
        this.arguments = "<status>";
        this.guildOnly = false;
    }

    @Override
    protected final void execute(CommandEvent event) {
        try {
            OnlineStatus status = OnlineStatus.fromKey(event.getArgs());
            if (status == OnlineStatus.UNKNOWN) {
                event.replyError("Please include one of the following statuses: `ONLINE`, `IDLE`, `DND`, `INVISIBLE`");
            } else {
                event.getJDA().getPresence().setStatus(status);
                event.replySuccess(String.format("Set the status to `%1$s`", status.getKey().toUpperCase()));
                log.info("Status of bot was changed to {} by {}", status.getKey().toUpperCase(), FormatUtils.formatAuthor(event));
            }
        } catch (Exception e) {
            event.replyError("The status could not be set!");
        }
    }
}
