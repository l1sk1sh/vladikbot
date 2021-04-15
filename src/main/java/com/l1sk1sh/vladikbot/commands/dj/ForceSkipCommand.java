package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class ForceSkipCommand extends DJCommand {

    @Autowired
    public ForceSkipCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "forceskip";
        this.aliases = new String[]{"modskip"};
        this.help = "skips the current song";
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        User user = event.getJDA().getUserById(Objects.requireNonNull(audioHandler).getRequester());
        event.replySuccess(String.format("Skipped **%1$s** (requested by *%2$s*).",
                audioHandler.getPlayer().getPlayingTrack().getInfo().title,
                (user == null ? "someone" : user.getName())));
        audioHandler.getPlayer().stopTrack();
    }
}
