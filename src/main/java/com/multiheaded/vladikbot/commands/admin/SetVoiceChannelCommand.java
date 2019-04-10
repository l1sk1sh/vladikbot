package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.multiheaded.vladikbot.models.SettingsFunction;
import com.multiheaded.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetVoiceChannelCommand extends AdminCommand {
    private final SettingsFunction<VoiceChannel> setVoiceChannelId;

    public SetVoiceChannelCommand(SettingsFunction<VoiceChannel> setVoiceChannelId) {
        this.name = "setvc";
        this.help = "sets the voice channel for playing music";
        this.arguments = "<channel|none>";
        this.setVoiceChannelId = setVoiceChannelId;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a voice channel or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            setVoiceChannelId.set(null);
            event.replySuccess("Music can now be played in any channel.");
        } else {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Voice Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfVoiceChannels(list, event.getArgs()));
            } else {
                setVoiceChannelId.set(list.get(0));
                event.replySuccess(String.format("Music can now only be played in **%1$s**.", list.get(0).getName()));
            }
        }
    }
}
