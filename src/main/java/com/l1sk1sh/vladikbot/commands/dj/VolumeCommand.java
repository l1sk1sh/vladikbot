package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public class VolumeCommand extends DJCommand {
    private static final int MAX_VOLUME = 150;

    private static final String VOLUME_OPTION_KEY = "volume";

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public VolumeCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "mvolume";
        this.help = "Sets or shows volume";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, VOLUME_OPTION_KEY, "New volume for player between [0 - " + MAX_VOLUME + "]").setRequired(false));
    }

    @Override
    public final void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        int currentVolume = Objects.requireNonNull(audioHandler).getAudioPlayer().getVolume();

        OptionMapping volumeOption = event.getOption(VOLUME_OPTION_KEY);
        if (volumeOption == null) {
            event.replyFormat("%1$s Current volume is `%2$s`.", FormatUtils.volumeIcon(currentVolume), currentVolume).setEphemeral(true).queue();

            return;
        }

        int newVolume = (int) volumeOption.getAsLong();

        if (newVolume < 0 || newVolume > MAX_VOLUME) {
            event.replyFormat("%1$s Volume must be a valid integer between 0 and `%2$d`!", event.getClient().getWarning(), MAX_VOLUME).setEphemeral(true).queue();
        } else {
            audioHandler.getAudioPlayer().setVolume(newVolume);
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(settings -> {
                settings.setVolume(newVolume);
                guildSettingsRepository.save(settings);
                event.replyFormat("%1$s Volume changed from `%2$s` to `%3$s`.",
                        FormatUtils.volumeIcon(newVolume), currentVolume, newVolume).queue();
            });
        }
    }
}
