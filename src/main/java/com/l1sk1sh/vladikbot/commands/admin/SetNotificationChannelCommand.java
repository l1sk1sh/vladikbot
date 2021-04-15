package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Service
public class SetNotificationChannelCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(SetNotificationChannelCommand.class);

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SetNotificationChannelCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "setnc";
        this.help = "sets the text channel for notifications from bot";
        this.arguments = "<channel|none>";
    }

    @Override
    protected final void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a text channel or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                setting.setNotificationChannelId(0L);
                guildSettingsRepository.save(setting);
                log.info("Notification channel was removed. Set by {}.", FormatUtils.formatAuthor(event));
                event.replySuccess("Bot-specific and technical notifications are disabled.");
            });
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Text Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfTextChannels(list, event.getArgs()));
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setNotificationChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("Notification channel was set to {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replySuccess(String.format("Notifications are being displayed in <#%1$s>.", list.get(0).getId()));
                });
            }
        }
    }
}
