package com.l1sk1sh.vladikbot.commands.dj;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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
public class SkipToCommand extends DJCommand {

    private static final String POSITION_OPTION_KEY = "position";

    @Autowired
    public SkipToCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "mskip_to";
        this.help = "Skips to the specified song";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, POSITION_OPTION_KEY, "Position of the song").setRequired(true));
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(SlashCommandEvent event) {
        OptionMapping positionOption = event.getOption(POSITION_OPTION_KEY);
        if (positionOption == null) {
            event.replyFormat("%1$s Please include song's position in the queue", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        int index = (int) positionOption.getAsLong();

        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if ((index < 1) || (index > Objects.requireNonNull(audioHandler).getQueue().size())) {
            event.replyFormat("%1$ Position must be a valid integer between 1 and %2$s!",
                    getClient().getWarning(),
                    Objects.requireNonNull(audioHandler).getQueue().size()
            ).setEphemeral(true).queue();

            return;
        }
        audioHandler.getQueue().skip(index - 1);
        event.replyFormat("%1$ Skipped to **%2$s**.", getClient().getWarning(), audioHandler.getQueue().get(0).getTrack().getInfo().title).queue();
        audioHandler.getPlayer().stopTrack();
    }
}
