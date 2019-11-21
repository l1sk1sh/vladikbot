package l1.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import l1.multiheaded.vladikbot.Bot;
import l1.multiheaded.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * @author Oliver Johnson
 */
public class SetNotificationChannelCommand extends AdminCommand {
    private final Bot bot;

    public SetNotificationChannelCommand(Bot bot) {
        this.name = "setnc";
        this.help = "sets the text channel for notifications from bot";
        this.arguments = "<channel|none>";
        this.bot = bot;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a text channel or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            bot.getGuildSettings(event.getGuild()).setNotificationChannelId(null);
            event.replySuccess("Bot-specific and technical notifications are disabled.");
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Text Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfTextChannels(list, event.getArgs()));
            } else {
                bot.getGuildSettings(event.getGuild()).setNotificationChannelId(list.get(0));
                event.replySuccess(String.format("Notifications are being displayed in <#%1$s>.", list.get(0).getId()));
            }
        }
    }

}
