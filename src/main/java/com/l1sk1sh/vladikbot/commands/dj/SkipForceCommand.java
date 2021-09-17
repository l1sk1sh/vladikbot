package com.l1sk1sh.vladikbot.commands.dj;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.entities.User;
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
public class SkipForceCommand extends DJCommand {

    @Autowired
    public SkipForceCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "mskip_force";
        this.help = "Skips the current song";
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        User user = event.getJDA().getUserById(Objects.requireNonNull(audioHandler).getRequester());
        event.replyFormat("Skipped **%1$s** (requested by *%2$s*).",
                audioHandler.getPlayer().getPlayingTrack().getInfo().title,
                (user == null ? "someone" : user.getName())).queue();
        audioHandler.getPlayer().stopTrack();
    }
}
