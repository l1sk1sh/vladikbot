package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.multiheaded.vladikbot.models.SettingsFunction;
import com.multiheaded.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetTextChannelCommand extends AdminCommand {
    private final SettingsFunction<TextChannel> setTextChannelId;

    public SetTextChannelCommand(SettingsFunction<TextChannel> setTextChannelId) {
        this.name = "settc";
        this.help = "sets the text channel for music commands";
        this.arguments = "<channel|NONE>";
        this.setTextChannelId = setTextChannelId;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " Please include a text channel or NONE");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            setTextChannelId.set(null);
            event.reply(event.getClient().getSuccess() + " Music commands can now be used in any channel");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.reply(event.getClient().getWarning()
                        + " No Text Channels found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
                event.reply(event.getClient().getWarning() + FormatUtils.listOfTChannels(list, event.getArgs()));
            } else {
                setTextChannelId.set(list.get(0));
                event.reply(event.getClient().getSuccess()
                        + " Music commands can now only be used in <#" + list.get(0).getId() + ">");
            }
        }
    }

}
