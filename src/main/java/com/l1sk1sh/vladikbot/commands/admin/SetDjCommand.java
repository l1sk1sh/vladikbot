package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetDjCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(SetDjCommand.class);
    private final Bot bot;

    public SetDjCommand(Bot bot) {
        this.name = "setdj";
        this.help = "sets the DJ role for certain music commands";
        this.arguments = "<rolename|none>";
        this.bot = bot;
    }

    @Override
    protected final void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a role name or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            bot.getGuildSettings(event.getGuild()).setDjRoleId(null);
            event.replySuccess("DJ role cleared.");
        } else {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Roles found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfRoles(list, event.getArgs()));
            } else {
                bot.getGuildSettings(event.getGuild()).setDjRoleId(list.get(0));
                log.info("DJ role now available for {}. Set by {}:[{}].", list.get(0).getName(), event.getAuthor().getName(), event.getAuthor().getId());
                event.replySuccess(String.format("DJ commands can now be used by users with the **%1$s** role.",
                        list.get(0).getName()));
            }
        }
    }

}
