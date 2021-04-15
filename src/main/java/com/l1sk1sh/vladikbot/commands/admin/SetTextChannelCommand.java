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
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class SetTextChannelCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(SetTextChannelCommand.class);

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SetTextChannelCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "settc";
        this.help = "sets the text channel for music commands";
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
                setting.setTextChannelId(0L);
                guildSettingsRepository.save(setting);
                log.info("Music commands can now be used in any channel. Set by {}.", FormatUtils.formatAuthor(event));
                event.replySuccess("Music commands can now be used in any channel.");
            });
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Text Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfTextChannels(list, event.getArgs()));
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setTextChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("Music commands now can be used only in {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replySuccess(String.format("Music commands can now only be used in <#%1$s>.", list.get(0).getId()));
                });
            }
        }
    }
}
