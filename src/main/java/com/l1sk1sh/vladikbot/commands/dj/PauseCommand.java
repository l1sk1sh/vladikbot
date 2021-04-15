package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
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
        this.name = "pause";
        this.help = "pauses the current song";
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (Objects.requireNonNull(audioHandler).getPlayer().isPaused()) {
            event.replyWarning(String.format("The player is already paused! Use `%1$splay` to unpause!",
                    event.getClient().getPrefix()));
            return;
        }
        audioHandler.getPlayer().setPaused(true);
        event.replySuccess(String.format("Paused **%1$s**. Type `%2$splay` to unpause!",
                audioHandler.getPlayer().getPlayingTrack().getInfo().title,
                event.getClient().getPrefix()));
    }
}
