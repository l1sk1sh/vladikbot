package com.l1sk1sh.vladikbot.commands.dj;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class PlayNextCommand extends DJCommand {

    private static final String SONG_OPTION_KEY = "song";

    private final BotSettingsManager settings;

    @Autowired
    public PlayNextCommand(BotSettingsManager settings, GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.settings = settings;
        this.name = "mplaynext";
        this.help = "Plays a single song next";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, SONG_OPTION_KEY, "Song's name or URL to be played next").setRequired(true));
        this.beListening = true;
        this.bePlaying = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        OptionMapping songOption = event.getOption(SONG_OPTION_KEY);
        if (songOption == null) {
            event.replyFormat("%1$s Please include a song name or URL.", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        String song = songOption.getAsString();

        if (song.isEmpty()) {
            event.replyFormat("%1$s Please include a song name or URL.", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        playerManager.loadItemOrdered(event.getGuild(), song, new ResultHandler(song, event, false));
    }

    private final class ResultHandler implements AudioLoadResultHandler {

        private final String song;
        private final SlashCommandEvent event;
        private final boolean ytsearch;

        private ResultHandler(String song, SlashCommandEvent event, boolean ytsearch) {
            this.song = song;
            this.event = event;
            this.ytsearch = ytsearch;
        }

        @SuppressWarnings("DuplicatedCode")
        private void loadSingle(AudioTrack track) {
            if (settings.get().isTooLong(track)) {
                event.replyFormat(
                        "%1$s This track (**%2$s**) is longer than the allowed maximum: `%3$s` > `%4$s`.",
                        getClient().getWarning(),
                        FormatUtils.filter(track.getInfo().title),
                        FormatUtils.formatTimeTillHours(track.getDuration()),
                        FormatUtils.formatTimeTillHours(settings.get().getMaxSeconds() * 1000)
                ).setEphemeral(true).queue();

                return;
            }

            AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
            int position = Objects.requireNonNull(audioHandler).addTrackToFront(new QueuedTrack(track, event.getUser())) + 1;

            event.replyFormat(
                    "%1$s Added **%2$s** (`%3$s`) %4$s.",
                    getClient().getSuccess(),
                    FormatUtils.filter(track.getInfo().title),
                    FormatUtils.formatTimeTillHours(track.getDuration()),
                    ((position == 0) ? "to begin playing" : " to the queue at position " + position)
            ).queue();
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            AudioTrack single;
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                single = (playlist.getSelectedTrack() == null)
                        ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
            } else if (playlist.getSelectedTrack() != null) {
                single = playlist.getSelectedTrack();
            } else {
                single = playlist.getTracks().get(0);
            }
            loadSingle(single);
        }

        @Override
        public void noMatches() {
            if (ytsearch) {
                event.replyFormat("%1$s  No results found for `%2$s`.",
                        getClient().getWarning(), FormatUtils.filter(song)).setEphemeral(true).queue();
            } else {
                playerManager.loadItemOrdered(event.getGuild(), Const.YT_SEARCH_PREFIX
                        + song, new ResultHandler(song, event, true));
            }
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == FriendlyException.Severity.COMMON) {
                event.replyFormat("%1$s  Error loading: %2$s.", getClient().getError(),
                        throwable.getLocalizedMessage()).setEphemeral(true).queue();
            } else {
                event.replyFormat("%1$s  Error loading track.", getClient().getError()).setEphemeral(true).queue();
            }
        }
    }
}
