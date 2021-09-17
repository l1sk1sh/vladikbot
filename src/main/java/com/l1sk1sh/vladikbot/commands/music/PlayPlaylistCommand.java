package com.l1sk1sh.vladikbot.commands.music;


import com.l1sk1sh.vladikbot.data.entity.Playlist;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.services.audio.PlaylistLoader;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
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
 * - separated from PlayCommand
 * @author John Grosh
 */
@Service
public class PlayPlaylistCommand extends MusicCommand {

    private static final String PLAYLIST_OPTION_NAME = "playlist";

    private final BotSettingsManager settings;
    private final PlaylistLoader playlistLoader;

    @Autowired
    public PlayPlaylistCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager,
                               PlaylistLoader playlistLoader, BotSettingsManager settings) {
        super(guildSettingsRepository, playerManager);
        this.playlistLoader = playlistLoader;
        this.settings = settings;
        this.name = "mplay_playlist";
        this.help = "Plays the provided playlist";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, PLAYLIST_OPTION_NAME, "Playlist to be played next").setRequired(true));
        this.beListening = true;
        this.bePlaying = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        OptionMapping optionPlaylist = event.getOption(PLAYLIST_OPTION_NAME);
        if (optionPlaylist == null) {
            event.replyFormat("%1$s Please include a playlist name.", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        String playlistName = optionPlaylist.getAsString();
        Playlist playlist = playlistLoader.getPlaylist(playlistName);
        if (playlist == null) {
            event.replyFormat("%1$s Could not find `%2$s` in the Playlists folder.", getClient().getError(), playlistName).setEphemeral(true).queue();

            return;
        }

        if (playlist.getItems().isEmpty() || (playlist.getItems() == null)) {
            event.replyFormat("%1$s Specified playlist is empty!", getClient().getError()).setEphemeral(true).queue();

            return;
        }

        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        playlistLoader.loadTracksIntoPlaylist(
                playlist,
                playerManager,
                (audioTrack) ->
                        Objects.requireNonNull(audioHandler).addTrack(new QueuedTrack(audioTrack, event.getUser())),
                () -> {
                    String errorMessage = getClient().getError() + " No tracks were loaded!";
                    String successMessage = getClient().getSuccess()
                            + " Loaded **" + playlist.getTracks().size() + "** tracks!";
                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                            ? errorMessage
                            : successMessage);

                    if (!playlist.getErrors().isEmpty()) {
                        builder.append("\r\n_The following tracks failed to load_:");
                    }

                    playlist.getErrors().forEach(
                            err -> builder.append("\r\n`[").append(err.getNumber() + 1).append("]` **")
                                    .append(err.getItem()).append("**: ").append(err.getReason())
                    );

                    String str = builder.toString();
                    if (str.length() > 2000) {
                        str = str.substring(0, 1994) + " (...)";
                    }

                    event.reply(FormatUtils.filter(str)).setEphemeral(!playlist.getTracks().isEmpty()).queue();
                }
        );
    }
}
