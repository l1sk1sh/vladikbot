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
        this.arguments = "<channel|none>";
        this.setTextChannelId = setTextChannelId;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a text channel or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            setTextChannelId.set(null);
            event.replySuccess("Music commands can now be used in any channel.");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Text Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfTextChannels(list, event.getArgs()));
            } else {
                setTextChannelId.set(list.get(0));
                event.replySuccess(String.format("Music commands can now only be used in <#%1$s>.", list.get(0).getId()));
            }
        }
    }

}
