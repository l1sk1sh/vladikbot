package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Slf4j
@Service
public class SetTextChannelCommand extends AdminCommand {

    private static final String CHANNEL_OPTION_KEY = "channel";

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SetTextChannelCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "settc";
        this.help = "Sets the text channel for music commands";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, CHANNEL_OPTION_KEY, "Text channel for music commands. Set 'none' to remove limits").setRequired(false));
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
        TextChannel textChannel = settings.map(guildSettings -> guildSettings.getTextChannel(event.getGuild())).orElse(null);

        OptionMapping channelOption = event.getOption(CHANNEL_OPTION_KEY);
        if (channelOption == null) {
            event.replyFormat("Text commands are limited to `%1$s`", (textChannel != null) ? textChannel.getAsMention() : "everywhere").setEphemeral(true).queue();

            return;
        }

        String textChannelId = channelOption.getAsString();
        if (textChannelId.equals("0") || textChannelId.equalsIgnoreCase("none")) {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                setting.setTextChannelId(0L);
                guildSettingsRepository.save(setting);
                log.info("Music commands can now be used in any channel. Set by {}.", FormatUtils.formatAuthor(event));
                event.replyFormat("%1$s Music commands can now be used in any channel.", event.getClient().getSuccess()).setEphemeral(true).queue();
            });
        } else {
            List<TextChannel> list = FinderUtil.findTextChannels(textChannelId, event.getGuild());
            if (list.isEmpty()) {
                event.replyFormat("%1$s No Text Channels found matching \"%2$s\".", textChannelId).setEphemeral(true).queue();
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setTextChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("Music commands now can be used only in {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s Music commands can now only be used in <#%2$s>.", event.getClient().getSuccess(), list.get(0).getId()).setEphemeral(true).queue();
                });
            }
        }
    }
}
