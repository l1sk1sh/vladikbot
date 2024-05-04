package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.AudioRequestMetadata;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
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

    private final BotSettingsManager settings;

    @Autowired
    public SkipCommand(BotSettingsManager settings, GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.settings = settings;
        this.name = "mskip";
        this.help = "Votes to skip the current song";
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        AudioRequestMetadata rm = Objects.requireNonNull(audioHandler).getRequestMetadata();
        if (event.getUser().getIdLong() == rm.getOwner()) {
            event.replyFormat("%1$s Skipped **%2$s**.",
                    event.getClient().getSuccess(), audioHandler.getAudioPlayer().getPlayingTrack().getInfo().title).queue();
            audioHandler.getAudioPlayer().stopTrack();
        } else {
            int listeners = (int) Objects.requireNonNull(Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel()).getMembers().stream()
                    .filter(member -> !member.getUser().isBot() && !Objects.requireNonNull(member.getVoiceState()).isDeafened()).count();
            String message;

            if (audioHandler.getVotes().contains(event.getUser().getIdLong())) {
                message = event.getClient().getWarning() + " You already voted to skip this song `[";
            } else {
                message = event.getClient().getSuccess() + " You voted to skip the song `[";
                audioHandler.getVotes().add(event.getUser().getIdLong());
            }

            int skippers = (int) Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState().getChannel()).getMembers().stream()
                    .filter(m -> audioHandler.getVotes().contains(m.getUser().getIdLong())).count();
            int required = (int) Math.ceil(listeners * settings.get().getAudioSkipRatio());
            message += skippers + " votes, " + required + "/" + listeners + " needed]`";

            if (skippers >= required) {
                message += String.format("\r\n%1$s Skipped **%2$s**%3$s.",
                        event.getClient().getSuccess(),
                        audioHandler.getAudioPlayer().getPlayingTrack().getInfo().title,
                        (rm.getOwner() == 0L
                                ? "(autoplay)"
                                : "(requested by **" + rm.getUser().getUsername() + "**)")
                );

                audioHandler.getAudioPlayer().stopTrack();
            }
            event.reply(message).queue();
        }
    }
}
