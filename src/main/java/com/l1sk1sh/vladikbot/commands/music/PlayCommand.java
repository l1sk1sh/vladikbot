package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.l1sk1sh.vladikbot.commands.dj.DJCommand;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Moving to JDA-Chewtils
 * - Moving Playlist command upper
 * @author John Grosh
 */
@Service
public class PlayCommand extends DJCommand {

    private static final String SONG_OPTION_KEY = "song";

    private final EventWaiter eventWaiter;
    private final BotSettingsManager settings;
    private final PlayerManager playerManager;

    @Autowired
    public PlayCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager,
                       EventWaiter eventWaiter, BotSettingsManager settings) {
        super(guildSettingsRepository, playerManager);
        this.eventWaiter = eventWaiter;
        this.settings = settings;
        this.playerManager = playerManager;
        this.name = "mplay";
        this.help = "Plays the provided song";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, SONG_OPTION_KEY, "Song's name or URL to be played next").setRequired(false));
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
            AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
            if (Objects.requireNonNull(audioHandler).getPlayer().getPlayingTrack() != null && audioHandler.getPlayer().isPaused()) {
                if (super.checkDJPermission(event)) {
                    audioHandler.getPlayer().setPaused(false);
                    event.replyFormat("Resumed **%1$s**.",
                            audioHandler.getPlayer().getPlayingTrack().getInfo().title).queue();
                } else {
                    event.replyFormat("%1$s Only DJs can unpause the player!", getClient().getWarning()).setEphemeral(true).queue();
                }

                return;
            }

            String reply = getClient().getWarning() + " Play Command:\r\n"
                    + "`" + name + " <song title>` - plays the first result from Youtube\r\n"
                    + "`" + name + " <URL>` - plays the provided song, playlist or stream\r\n"
                    + CommandUtils.getListOfChildCommands(this, children, name).toString();
            event.reply(reply).setEphemeral(true).queue();

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
        private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
            if (settings.get().isTooLong(track)) {
                event.getHook().editOriginalFormat(
                        "%1$s This track (**%2$s**) is longer than the allowed maximum: `%3$s` > `%4$s`.",
                        getClient().getWarning(),
                        FormatUtils.filter(track.getInfo().title),
                        FormatUtils.formatTimeTillHours(track.getDuration()),
                        FormatUtils.formatTimeTillHours(settings.get().getMaxSeconds() * 1000)
                ).queue();

                return;
            }

            AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
            int position = Objects.requireNonNull(audioHandler).addTrack(new QueuedTrack(track, event.getUser())) + 1;

            String addMessage = FormatUtils.filter(String.format(
                    "%1$s Added **%2$s** (`%3$s`) %4$s.",
                    getClient().getSuccess(),
                    track.getInfo().title,
                    FormatUtils.formatTimeTillHours(track.getDuration()),
                    ((position == 0) ? "to begin playing" : " to the queue at position " + position))
            );

            if ((playlist == null)
                    || !event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ADD_REACTION)) {
                event.reply(addMessage).queue();
            } else {
                new ButtonMenu.Builder().setText(String.format(
                        "%1$s \r\n %2$s  This track has a playlist of **%3$s** tracks attached. Select %4$s to load playlist.",
                        addMessage,
                        getClient().getWarning(),
                        playlist.getTracks().size(),
                        Const.LOAD_EMOJI))
                        .setChoices(Const.LOAD_EMOJI, Const.CANCEL_EMOJI)
                        .setEventWaiter(eventWaiter)
                        .setTimeout(30, TimeUnit.SECONDS)
                        .setAction(re ->
                        {
                            if (re.getName().equals(Const.LOAD_EMOJI)) {
                                event.replyFormat("%1$s \r\n %2$s Loaded **%3$s** additional tracks!",
                                        addMessage,
                                        getClient().getSuccess(),
                                        loadPlaylist(playlist, track)
                                ).queue();
                            } else {
                                event.reply(addMessage).queue();
                            }
                        }).setFinalAction(m ->
                {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignored) {
                    }
                }).build().display(event.getMessageChannel());
            }
        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            int[] count = {0};
            playlist.getTracks().forEach((track) -> {
                if (!settings.get().isTooLong(track) && !track.equals(exclude)) {
                    AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
                    Objects.requireNonNull(audioHandler).addTrack(new QueuedTrack(track, event.getUser()));
                    count[0]++;
                }
            });
            return count[0];
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track, null);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if ((playlist.getTracks().size() == 1) || playlist.isSearchResult()) {
                AudioTrack single = (playlist.getSelectedTrack() == null)
                        ? playlist.getTracks().get(0)
                        : playlist.getSelectedTrack();
                loadSingle(single, null);
            } else if (playlist.getSelectedTrack() != null) {
                AudioTrack single = playlist.getSelectedTrack();
                loadSingle(single, playlist);
            } else {
                int count = loadPlaylist(playlist, null);
                if (count == 0) {
                    event.replyFormat(
                            "%1$s All entries in this playlist %2$s were longer than the allowed maximum (`%3$s`).",
                            getClient().getWarning(),
                            FormatUtils.filter((playlist.getName() == null) ? "" : "(**" + playlist.getName() + "**)"),
                            settings.get().getMaxTime()
                    ).queue();
                } else {
                    event.replyFormat(
                            "%1$s Found %2$s with `%3$s` entries; added to the queue! %4$s",
                            getClient().getSuccess(),
                            FormatUtils.filter((playlist.getName() == null) ? "a playlist" : "playlist **" + playlist.getName() + "**"),
                            playlist.getTracks().size(),
                            ((count < playlist.getTracks().size())
                                    ? String.format("\r\n%1$s Tracks longer than the allowed maximum (`%2$s`) have been omitted.",
                                    getClient().getWarning(),
                                    settings.get().getMaxTime())
                                    : "")
                    ).queue();
                }
            }
        }

        @Override
        public void noMatches() {
            if (ytsearch) {
                event.replyFormat("%1$s No results found for `%2$s`.",
                        getClient().getWarning(), FormatUtils.filter(song)).setEphemeral(true).queue();
            } else {
                playerManager.loadItemOrdered(event.getGuild(), Const.YT_SEARCH_PREFIX
                        + song, new ResultHandler(song, event, true));
            }
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON) {
                event.replyFormat("%1$s Error loading: %2$s.", getClient().getError(),
                        throwable.getLocalizedMessage()).setEphemeral(true).queue();
            } else {
                event.replyFormat("%1$s Error loading track.", getClient().getError()).setEphemeral(true).queue();
            }
        }
    }
}
