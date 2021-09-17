package com.l1sk1sh.vladikbot.commands.dj;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class PauseCommand extends DJCommand {

    @Autowired
    public PauseCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "mpause";
        this.help = "Pauses the current song";
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if (Objects.requireNonNull(audioHandler).getPlayer().isPaused()) {
            event.replyFormat("%1$s The player is already paused.", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }
        audioHandler.getPlayer().setPaused(true);
        event.replyFormat("Paused **%1$s**.", audioHandler.getPlayer().getPlayingTrack().getInfo().title).queue();
    }
}
