package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetTextChannelCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(SetTextChannelCommand.class);
    private final Bot bot;

    public SetTextChannelCommand(Bot bot) {
        this.name = "settc";
        this.help = "sets the text channel for music commands";
        this.arguments = "<channel|none>";
        this.bot = bot;
    }

    @Override
    protected final void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a text channel or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            bot.getGuildSettings(event.getGuild()).setTextChannelId(null);
            event.replySuccess("Music commands can now be used in any channel.");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Text Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfTextChannels(list, event.getArgs()));
            } else {
                bot.getGuildSettings(event.getGuild()).setTextChannelId(list.get(0));
                log.info("Music commands now can be used only in {}. Set by {}:[{}]", list.get(0).getId(), event.getAuthor().getName(), event.getAuthor().getId());
                event.replySuccess(String.format("Music commands can now only be used in <#%1$s>.", list.get(0).getId()));
            }
        }
    }

}
