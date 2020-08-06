package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Oliver Johnson
 */
public class MemesManagementCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(MemesManagementCommand.class);
    private final Bot bot;

    public MemesManagementCommand(Bot bot) {
        this.bot = bot;
        this.name = "memes";
        this.help = "Manage memes for this guild";
        this.arguments = "<switch|setch>";
        this.children = new AdminCommand[]{
                new SwitchCommand(),
                new SetChannelCommand()
        };
    }

    @Override
    protected final void execute(CommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
    }

    private final class SwitchCommand extends AdminCommand {
        SwitchCommand() {
            this.name = "switch";
            this.aliases = new String[]{"change"};
            this.help = "enables or disables memes update";
            this.arguments = "<on|off>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length > 0) {
                for (String arg : args) {
                    switch (arg) {
                        case "on":
                        case "enable":
                            bot.getBotSettings().setSendMemes(true);
                            event.replySuccess("Memes feed is now enabled!");
                            bot.getMemeService().start();
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setSendMemes(false);
                            event.replySuccess("Memes feed is now disabled!");
                            bot.getMemeService().stop();
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    private final class SetChannelCommand extends AdminCommand {
        SetChannelCommand() {
            this.name = "setch";
            this.help = "sets channel for memes submission";
            this.arguments = "<channel>";
        }

        @Override
        protected final void execute(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include a text channel.");
                return;
            }

            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Text Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfTextChannels(list, event.getArgs()));
            } else {
                bot.getGuildSettings(event.getGuild()).setMemesChannelId(list.get(0));
                log.info("Memes channel was set to {}. Set by {}:[{}].", list.get(0).getId(), event.getAuthor().getName(), event.getAuthor().getId());
                event.replySuccess(String.format("Memes are being displayed in <#%1$s>.", list.get(0).getId()));
            }
        }
    }
}
