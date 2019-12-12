package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetVoiceChannelCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(SetVoiceChannelCommand.class);
    private final Bot bot;

    public SetVoiceChannelCommand(Bot bot) {
        this.name = "setvc";
        this.help = "sets the voice channel for playing music";
        this.arguments = "<channel|none>";
        this.bot = bot;
    }

    @Override
    protected final void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a voice channel or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            bot.getGuildSettings(event.getGuild()).setVoiceChannelId(null);
            event.replySuccess("Music can now be played in any channel.");
        } else {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Voice Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfVoiceChannels(list, event.getArgs()));
            } else {
                bot.getGuildSettings(event.getGuild()).setVoiceChannelId(list.get(0));
                log.info("Music can be used played in {}. Set by {}:[{}].", list.get(0).getId(), event.getAuthor().getName(), event.getAuthor().getId());
                event.replySuccess(String.format("Music can now only be played in **%1$s**.", list.get(0).getName()));
            }
        }
    }
}
