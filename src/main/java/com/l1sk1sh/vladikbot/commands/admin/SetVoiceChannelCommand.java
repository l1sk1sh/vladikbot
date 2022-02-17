package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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
public class SetVoiceChannelCommand extends AdminCommand {

    private static final String CHANNEL_OPTION_KEY = "channel";

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public SetVoiceChannelCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "setvc";
        this.help = "Sets the voice channel for playing music";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, CHANNEL_OPTION_KEY, "Voice channel for music commands. Set 'none' to remove one").setRequired(false));
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
        VoiceChannel voiceChannel = settings.map(guildSettings -> guildSettings.getVoiceChannel(event.getGuild())).orElse(null);

        OptionMapping channelOption = event.getOption(CHANNEL_OPTION_KEY);
        if (channelOption == null) {
            event.replyFormat("Music is limited to `%1$s`", (voiceChannel != null) ? voiceChannel.getAsMention() : "any channel").setEphemeral(true).queue();

            return;
        }

        String voiceChannelId = channelOption.getAsString();
        if (voiceChannelId.equals("0") || voiceChannelId.equalsIgnoreCase("none")) {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                setting.setVoiceChannelId(0L);
                guildSettingsRepository.save(setting);
                log.info("Music can now be played in any channel. Set by {}.", FormatUtils.formatAuthor(event));
                event.replyFormat("%1$s Music can now be played in any channel.", getClient().getSuccess()).setEphemeral(true).queue();
            });
        } else {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(voiceChannelId, event.getGuild());
            if (list.isEmpty()) {
                event.replyFormat("%1$s No Voice Channels found matching \"%2$s\".", getClient().getWarning(), voiceChannelId).setEphemeral(true).queue();
            } else {
                guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(setting -> {
                    setting.setVoiceChannelId(list.get(0).getIdLong());
                    guildSettingsRepository.save(setting);
                    log.info("Music can be used played in {}. Set by {}.", list.get(0).getId(), FormatUtils.formatAuthor(event));
                    event.replyFormat("%1$s Music can now only be played in **%2$s**.", getClient().getSuccess(), list.get(0).getAsMention()).setEphemeral(true).queue();
                });
            }
        }
    }
}
