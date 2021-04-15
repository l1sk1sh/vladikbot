package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.entities.User;
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
public class SkipCommand extends MusicCommand {

    @Autowired
    public SkipCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "skip";
        this.aliases = new String[]{"voteskip"};
        this.help = "votes to skip the current song";
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (event.getAuthor().getIdLong() == Objects.requireNonNull(audioHandler).getRequester()) {
            event.reply(String.format("%1$s Skipped **%2$s**.",
                    event.getClient().getSuccess(), audioHandler.getPlayer().getPlayingTrack().getInfo().title));
            audioHandler.getPlayer().stopTrack();
        } else {
            int listeners = (int) Objects.requireNonNull(Objects.requireNonNull(event.getSelfMember().getVoiceState()).getChannel()).getMembers().stream()
                    .filter(member -> !member.getUser().isBot() && !Objects.requireNonNull(member.getVoiceState()).isDeafened()).count();
            String message;

            if (audioHandler.getVotes().contains(event.getAuthor().getIdLong())) {
                message = event.getClient().getWarning() + " You already voted to skip this song `[";
            } else {
                message = event.getClient().getSuccess() + " You voted to skip the song `[";
                audioHandler.getVotes().add(event.getAuthor().getIdLong());
            }

            int skippers = (int) Objects.requireNonNull(event.getSelfMember().getVoiceState().getChannel()).getMembers().stream()
                    .filter(m -> audioHandler.getVotes().contains(m.getUser().getIdLong())).count();
            int required = (int) Math.ceil(listeners * .55);
            message += skippers + " votes, " + required + "/" + listeners + " needed]`";

            if (skippers >= required) {
                User user = event.getJDA().getUserById(audioHandler.getRequester());
                message += String.format("\r\n%1$s Skipped **%2$s**%3$s.",
                        event.getClient().getSuccess(),
                        audioHandler.getPlayer().getPlayingTrack().getInfo().title,
                        ((audioHandler.getRequester() == 0)
                                ? ""
                                : String.format(" (requested by %1$s",
                                ((user == null) ? "someone" : "**" + user.getName() + "**") + ")")
                        )
                );

                audioHandler.getPlayer().stopTrack();
            }
            event.reply(message);
        }
    }

}
