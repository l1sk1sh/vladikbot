package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
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
public class SetNameCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(SetNameCommand.class);

    @Autowired
    public SetNameCommand() {
        this.name = "setname";
        this.help = "sets the name of the bot";
        this.arguments = "<name>";
        this.guildOnly = false;
    }

    @Override
    protected final void execute(CommandEvent event) {
        try {
            String oldName = event.getSelfUser().getName();
            event.getSelfUser().getManager().setName(event.getArgs()).complete(false);
            event.replySuccess(String.format("Name changed from `%1$s` to `%2$s`.", oldName, event.getArgs()));
            log.info("Name of bot was changed to {} by {}", event.getArgs(), FormatUtils.formatAuthor(event));
        } catch (RateLimitedException e) {
            event.replyError("Name can only be changed twice per hour!");
        } catch (Exception e) {
            event.replyError("That name is not valid!");
        }
    }
}
