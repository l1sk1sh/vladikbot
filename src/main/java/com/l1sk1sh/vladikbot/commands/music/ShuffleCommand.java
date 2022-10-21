package com.l1sk1sh.vladikbot.commands.music;

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
public class ShuffleCommand extends MusicCommand {

    @Autowired
    public ShuffleCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "mshuffle";
        this.help = "Shuffles songs you have added";
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        int shuffle = Objects.requireNonNull(audioHandler).getQueue().shuffle(event.getUser().getIdLong());
        switch (shuffle) {
            case 0:
                event.replyFormat("%1$s You don't have any music in the queue to shuffle!", event.getClient().getError()).setEphemeral(true).queue();
                break;
            case 1:
                event.replyFormat("%1$s You only have one song in the queue!", event.getClient().getWarning()).setEphemeral(true).queue();
                break;
            default:
                event.replyFormat("%1$s You successfully shuffled your entries.", event.getClient().getSuccess()).queue();
                break;
        }
    }
}
