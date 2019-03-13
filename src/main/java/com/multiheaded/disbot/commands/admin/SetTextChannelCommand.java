package com.multiheaded.disbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.utils.FormatUtil;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SetTextChannelCommand extends AdminCommand {
    public SetTextChannelCommand() {
        this.name = "settc";
        this.help = "sets the text channel for music commands";
        this.arguments = "<channel|NONE>";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " Please include a text channel or NONE");
            return;
        }
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().equalsIgnoreCase("none")) {
            settings.setTextChannelId(null);
            event.reply(event.getClient().getSuccess() + " Music commands can now be used in any channel");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.reply(event.getClient().getWarning()
                        + " No Text Channels found matching \"" + event.getArgs() + "\"");
            } else if (list.size() > 1) {
                event.reply(event.getClient().getWarning() + FormatUtil.listOfTChannels(list, event.getArgs()));
            } else {
                settings.setTextChannelId(list.get(0));
                event.reply(event.getClient().getSuccess()
                        + " Music commands can now only be used in <#" + list.get(0).getId() + ">");
            }
        }
    }

}
