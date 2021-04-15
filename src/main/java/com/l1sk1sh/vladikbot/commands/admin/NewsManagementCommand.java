package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.rss.RssService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Oliver Johnson
 */
@Service
public class NewsManagementCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(NewsManagementCommand.class);

    private final BotSettingsManager settings;
    private final RssService rssService;
    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public NewsManagementCommand(BotSettingsManager settings, RssService rssService, GuildSettingsRepository guildSettingsRepository) {
        this.settings = settings;
        this.rssService = rssService;
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "news";
        this.help = "Manage news for this guild";
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
        private SwitchCommand() {
            this.name = "switch";
            this.aliases = new String[]{"change"};
            this.help = "enables or disables news";
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
                            settings.get().setSendNews(true);
                            event.replySuccess("News feed is now enabled!");
                            rssService.start();
                            // Add another news services here
                            break;
                        case "off":
                        case "disable":
                            settings.get().setSendNews(false);
                            event.replySuccess("News feed is now disabled!");
                            rssService.stop();
                            // Add another news services here
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    private final class SetChannelCommand extends AdminCommand {
        private SetChannelCommand() {
            this.name = "setch";
            this.help = "sets channel for news submission";
            this.arguments = "<channel>";
        }

        @Override
        protected void execute(CommandEvent event) {
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
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setNewsChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("News channel was set to {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replySuccess(String.format("News are being displayed in <#%1$s>.", list.get(0).getId()));
                });
            }
        }
    }
}
