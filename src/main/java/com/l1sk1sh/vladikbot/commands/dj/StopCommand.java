package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
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
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public class StopCommand extends DJCommand {

    @Autowired
    public StopCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "mstop";
        this.help = "Stops the current song and clears the queue";
        this.bePlaying = false;
    }

    @Override
    public final void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        Objects.requireNonNull(audioHandler).stopAndClear();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.replyFormat("%1$ The player has stopped and the queue has been cleared.", event.getClient().getSuccess()).queue();
    }
}
