package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.multiheaded.vladikbot.models.SettingsFunction;
import com.multiheaded.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * @author Oliver Johnson
 */
public class SetNotificationChannelCommand extends AdminCommand {
    private final SettingsFunction<TextChannel> setNotificationChannel;

    public SetNotificationChannelCommand(SettingsFunction<TextChannel> setNotificationChannel) {
        this.name = "setnc";
        this.help = "sets the text channel for notifications from bot";
        this.arguments = "<channel|none>";
        this.setNotificationChannel = setNotificationChannel;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a text channel or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            setNotificationChannel.set(null);
            event.replySuccess("Bot-specific and technical notifications are disabled.");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Text Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfTextChannels(list, event.getArgs()));
            } else {
                setNotificationChannel.set(list.get(0));
                event.replySuccess(String.format("Notifications are being displayed in <#%1$s>.", list.get(0).getId()));
            }
        }
    }

}
