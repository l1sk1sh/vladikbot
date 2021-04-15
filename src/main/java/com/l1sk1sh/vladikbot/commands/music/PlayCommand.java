package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.l1sk1sh.vladikbot.commands.dj.DJCommand;
import com.l1sk1sh.vladikbot.data.entity.Playlist;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.services.audio.PlaylistLoader;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class PlayCommand extends DJCommand {

    private final EventWaiter eventWaiter;
    private final BotSettingsManager settings;
    private final PlayerManager playerManager;
    private final PlaylistLoader playlistLoader;

    @Autowired
    public PlayCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager,
                       EventWaiter eventWaiter, BotSettingsManager settings, PlaylistLoader playlistLoader) {
        super(guildSettingsRepository, playerManager);
        this.eventWaiter = eventWaiter;
        this.settings = settings;
        this.playerManager = playerManager;
        this.playlistLoader = playlistLoader;
        this.name = "play";
        this.help = "plays the provided song";
        this.arguments = "<title|URL|sub command>";
        this.beListening = true;
        this.bePlaying = false;
        this.children = new Command[]{new PlaylistCommand(guildSettingsRepository, playerManager)};
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (Objects.requireNonNull(audioHandler).getPlayer().getPlayingTrack() != null && audioHandler.getPlayer().isPaused()) {
                if (super.checkDJPermission(event)) {
                    audioHandler.getPlayer().setPaused(false);
                    event.replySuccess(String.format("Resumed **%1$s**.",
                            audioHandler.getPlayer().getPlayingTrack().getInfo().title));
                } else {
                    event.replyError("Only DJs can unpause the player!");
                }
                return;
            }

            String message = event.getClient().getWarning() + " Play Commands:\r\n";
            StringBuilder builder = new StringBuilder(message);
            builder.append("\r\n`").append(event.getClient().getPrefix()).append(name)
                    .append(" <song title>` - plays the first result from Youtube");
            builder.append("\r\n`").append(event.getClient().getPrefix()).append(name)
                    .append(" <URL>` - plays the provided song, playlist, or stream");
            for (Command cmd : children) {
                builder.append("\r\n`").append(event.getClient().getPrefix()).append(name)
                        .append(" ").append(cmd.getName()).append(" ")
                        .append(cmd.getArguments()).append("` - ").append(cmd.getHelp());
            }
            event.reply(builder.toString());
            return;
        }
        String args = (event.getArgs().startsWith("<") && event.getArgs().endsWith(">"))
                ? event.getArgs().substring(1, event.getArgs().length() - 1)
                : ((event.getArgs().isEmpty())
                ? event.getMessage().getAttachments().get(0).getUrl()
                : event.getArgs());
        event.reply(String.format("%1$s Loading... `[%2$s]`", settings.get().getLoadingEmoji(), args),
                m -> playerManager.loadItemOrdered(
                        event.getGuild(), args, new ResultHandler(m, event, false)
                )
        );
    }

    private final class ResultHandler implements AudioLoadResultHandler {
        private final Message message;
        private final CommandEvent event;
        private final boolean ytsearch;

        private ResultHandler(Message message, CommandEvent event, boolean ytsearch) {
            this.message = message;
            this.event = event;
            this.ytsearch = ytsearch;
        }

        @SuppressWarnings("DuplicatedCode")
        private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
            if (settings.get().isTooLong(track)) {
                message.editMessage(FormatUtils.filter(String.format(
                        "%1$s This track (**%2$s**) is longer than the allowed maximum: `%3$s` > `%4$s`.",
                        event.getClient().getWarning(),
                        track.getInfo().title,
                        FormatUtils.formatTimeTillHours(track.getDuration()),
                        FormatUtils.formatTimeTillHours(settings.get().getMaxSeconds() * 1000)))
                ).queue();
                return;
            }
            AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = Objects.requireNonNull(audioHandler).addTrack(new QueuedTrack(track, event.getAuthor())) + 1;

            String addMessage = FormatUtils.filter(String.format(
                    "%1$s Added **%2$s** (`%3$s`) %4$s.",
                    event.getClient().getSuccess(),
                    track.getInfo().title,
                    FormatUtils.formatTimeTillHours(track.getDuration()),
                    ((pos == 0) ? "to begin playing" : " to the queue at position " + pos))
            );
            if ((playlist == null)
                    || !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ADD_REACTION)) {
                message.editMessage(addMessage).queue();
            } else {
                new ButtonMenu.Builder().setText(String.format(
                        "%1$s \r\n %2$s  This track has a playlist of **%3$s** tracks attached. Select %4$s to load playlist.",
                        addMessage,
                        event.getClient().getWarning(),
                        playlist.getTracks().size(),
                        Const.LOAD_EMOJI))
                        .setChoices(Const.LOAD_EMOJI, Const.CANCEL_EMOJI)
                        .setEventWaiter(eventWaiter)
                        .setTimeout(30, TimeUnit.SECONDS)
                        .setAction(re ->
                        {
                            if (re.getName().equals(Const.LOAD_EMOJI)) {
                                message.editMessage(String.format("%1$s \r\n %2$s Loaded **%3$s** additional tracks!",
                                        addMessage, event.getClient().getSuccess(), loadPlaylist(playlist, track)))
                                        .queue();
                            } else {
                                message.editMessage(addMessage).queue();
                            }
                        }).setFinalAction(m ->
                {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignored) {
                    }
                }).build().display(message);
            }
        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            int[] count = {0};
            playlist.getTracks().forEach((track) -> {
                if (!settings.get().isTooLong(track) && !track.equals(exclude)) {
                    AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                    Objects.requireNonNull(audioHandler).addTrack(new QueuedTrack(track, event.getAuthor()));
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
                    message.editMessage(FormatUtils.filter(String.format(
                            "%1$s All entries in this playlist %2$s were longer than the allowed maximum (`%3$s`).",
                            event.getClient().getWarning(),
                            ((playlist.getName() == null) ? "" : "(**" + playlist.getName() + "**)"),
                            settings.get().getMaxTime()))).queue();
                } else {
                    message.editMessage(FormatUtils.filter(String.format(
                            "%1$s Found %2$s with `%3$s` entries; added to the queue! %4$s",
                            event.getClient().getSuccess(),
                            ((playlist.getName() == null) ? "a playlist" : "playlist **" + playlist.getName() + "**"),
                            playlist.getTracks().size(),
                            ((count < playlist.getTracks().size())
                                    ? String.format("\r\n%1$s Tracks longer than the allowed maximum (`%2$s`) have been omitted.",
                                    event.getClient().getWarning(),
                                    settings.get().getMaxTime())
                                    : "")))
                    ).queue();
                }
            }
        }

        @Override
        public void noMatches() {
            if (ytsearch) {
                message.editMessage(FormatUtils.filter(String.format("%1$s  No results found for `%2$s`.",
                        event.getClient().getWarning(), event.getArgs()))).queue();
            } else {
                playerManager.loadItemOrdered(event.getGuild(), Const.YT_SEARCH_PREFIX
                        + event.getArgs(), new ResultHandler(message, event, true));
            }
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON) {
                message.editMessage(String.format("%1$s  Error loading: %2$s.", event.getClient().getError(),
                        throwable.getLocalizedMessage())).queue();
            } else {
                message.editMessage(String.format("%1$s  Error loading track.", event.getClient().getError())).queue();
            }
        }
    }

    private final class PlaylistCommand extends MusicCommand {
        private PlaylistCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
            super(guildSettingsRepository, playerManager);
            this.name = "playlist";
            this.aliases = new String[]{"pl"};
            this.arguments = "<name>";
            this.help = "plays the provided playlist";
            this.beListening = true;
            this.bePlaying = false;
        }

        @Override
        public void doCommand(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include a playlist name.");
                return;
            }

            Playlist playlist = playlistLoader.getPlaylist(event.getArgs());
            if (playlist == null) {
                event.replyError(String.format("I could not find `%1$s` in the Playlists folder.", event.getArgs()));
                return;
            }

            if (playlist.getItems().isEmpty() || (playlist.getItems() == null)) {
                event.replyWarning("Specified playlist is empty!");
            }

            event.getChannel().sendMessage(String.format("%1$s Loading playlist... **%2$s**... (%3$s items).",
                    settings.get().getLoadingEmoji(),
                    event.getArgs(),
                    playlist.getItems().size())).queue(m ->
            {
                AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

                playlistLoader.loadTracksIntoPlaylist(
                        playlist,
                        playerManager,
                        (audioTrack) -> Objects.requireNonNull(audioHandler).addTrack(new QueuedTrack(audioTrack, event.getAuthor())),
                        () -> {
                            String errorMessage = event.getClient().getWarning() + " No tracks were loaded!";
                            String successMessage = event.getClient().getSuccess()
                                    + " Loaded **" + playlist.getTracks().size() + "** tracks!";
                            StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                                    ? errorMessage
                                    : successMessage);

                            if (!playlist.getErrors().isEmpty()) {
                                builder.append("\r\nThe following tracks failed to load:");
                            }

                            playlist.getErrors().forEach(
                                    err -> builder.append("\r\n`[").append(err.getNumber() + 1).append("]` **")
                                            .append(err.getItem()).append("**: ").append(err.getReason())
                            );

                            String str = builder.toString();
                            if (str.length() > 2000) {
                                str = str.substring(0, 1994) + " (...)";
                            }
                            m.editMessage(FormatUtils.filter(str)).queue();
                        }
                );
            });
        }
    }
}
