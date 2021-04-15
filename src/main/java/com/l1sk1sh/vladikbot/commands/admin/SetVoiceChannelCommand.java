package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.entities.VoiceChannel;
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
public class SetVoiceChannelCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(SetVoiceChannelCommand.class);

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SetVoiceChannelCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "setvc";
        this.help = "sets the voice channel for playing music";
        this.arguments = "<channel|none>";
    }

    @Override
    protected final void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a voice channel or *none*.");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                setting.setVoiceChannelId(0L);
                guildSettingsRepository.save(setting);
                log.info("Music can now be played in any channel. Set by {}.", FormatUtils.formatAuthor(event));
                event.replySuccess("Music can now be played in any channel.");
            });
        } else {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
            if (list.isEmpty()) {
                event.replyWarning(String.format("No Voice Channels found matching \"%1$s\".", event.getArgs()));
            } else if (list.size() > 1) {
                event.replyWarning(FormatUtils.listOfVoiceChannels(list, event.getArgs()));
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setVoiceChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("Music can be used played in {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replySuccess(String.format("Music can now only be played in **%1$s**.", list.get(0).getName()));
                });
            }
        }
    }
}
