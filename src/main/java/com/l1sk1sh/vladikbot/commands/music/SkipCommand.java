package com.l1sk1sh.vladikbot.commands.music;

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
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public class SkipCommand extends MusicCommand {

    @Autowired
    public SkipCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "mskip";
        this.help = "Votes to skip the current song";
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if (event.getUser().getIdLong() == Objects.requireNonNull(audioHandler).getRequester()) {
            event.replyFormat("%1$s Skipped **%2$s**.",
                    getClient().getSuccess(), audioHandler.getPlayer().getPlayingTrack().getInfo().title).queue();
            audioHandler.getPlayer().stopTrack();
        } else {
            int listeners = (int) Objects.requireNonNull(Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel()).getMembers().stream()
                    .filter(member -> !member.getUser().isBot() && !Objects.requireNonNull(member.getVoiceState()).isDeafened()).count();
            String message;

            if (audioHandler.getVotes().contains(event.getUser().getIdLong())) {
                message = getClient().getWarning() + " You already voted to skip this song `[";
            } else {
                message = getClient().getSuccess() + " You voted to skip the song `[";
                audioHandler.getVotes().add(event.getUser().getIdLong());
            }

            int skippers = (int) Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState().getChannel()).getMembers().stream()
                    .filter(m -> audioHandler.getVotes().contains(m.getUser().getIdLong())).count();
            int required = (int) Math.ceil(listeners * .55);
            message += skippers + " votes, " + required + "/" + listeners + " needed]`";

            if (skippers >= required) {
                User user = event.getJDA().getUserById(audioHandler.getRequester());
                message += String.format("\r\n%1$s Skipped **%2$s**%3$s.",
                        getClient().getSuccess(),
                        audioHandler.getPlayer().getPlayingTrack().getInfo().title,
                        ((audioHandler.getRequester() == 0)
                                ? ""
                                : String.format(" (requested by %1$s",
                                ((user == null) ? "someone" : "**" + user.getName() + "**") + ")")
                        )
                );

                audioHandler.getPlayer().stopTrack();
            }
            event.reply(message).queue();
        }
    }
}
