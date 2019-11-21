package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.l1sk1sh.vladikbot.services.PlaylistLoader;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.settings.Constants;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.commands.dj.DJCommand;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class PlayCommand extends MusicCommand {
    public PlayCommand(Bot bot) {
        super(bot);
        this.name = "play";
        this.help = "plays the provided song";
        this.arguments = "<title|URL|sub command>";
        this.beListening = true;
        this.bePlaying = false;
        this.children = new Command[]{new PlaylistCommand(bot)};
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (audioHandler.getPlayer().getPlayingTrack() != null && audioHandler.getPlayer().isPaused()) {
                if (DJCommand.checkDJPermission(event)) {
                    audioHandler.getPlayer().setPaused(false);
                    event.replySuccess(String.format("Resumed **%1$s**.",
                            audioHandler.getPlayer().getPlayingTrack().getInfo().title));
                } else {
                    event.replyError("Only DJs can unpause the player!");
                }
                return;
            }

            StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Play Commands:\r\n");
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
        event.reply(String.format("%1$s Loading... `[%2$s]`", bot.getBotSettings().getLoadingEmoji(), args),
                m -> bot.getPlayerManager().loadItemOrdered(
                        event.getGuild(), args, new ResultHandler(m, event, false)
                )
        );
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message message;
        private final CommandEvent event;
        private final boolean ytsearch;

        private ResultHandler(Message message, CommandEvent event, boolean ytsearch) {
            this.message = message;
            this.event = event;
            this.ytsearch = ytsearch;
        }

        private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
            if (bot.getBotSettings().isTooLong(track)) {
                message.editMessage(FormatUtils.filter(String.format(
                        "%1$s This track (**%2$s**) is longer than the allowed maximum: `%3$s` > `%4$s`.",
                        event.getClient().getWarning(),
                        track.getInfo().title,
                        FormatUtils.formatTime(track.getDuration()),
                        FormatUtils.formatTime(bot.getBotSettings().getMaxSeconds() * 1000)))
                ).queue();
                return;
            }
            AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = audioHandler.addTrack(new QueuedTrack(track, event.getAuthor())) + 1;

            String addMessage = FormatUtils.filter(String.format(
                    "%1$s Added **%2$s** (`%3$s`) %4$s.",
                    event.getClient().getSuccess(),
                    track.getInfo().title,
                    FormatUtils.formatTime(track.getDuration()),
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
                        Constants.LOAD_EMOJI))
                        .setChoices(Constants.LOAD_EMOJI, Constants.CANCEL_EMOJI)
                        .setEventWaiter(bot.getWaiter())
                        .setTimeout(30, TimeUnit.SECONDS)
                        .setAction(re ->
                        {
                            if (re.getName().equals(Constants.LOAD_EMOJI)) {
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
                    } catch (PermissionException ignore) {
                    }
                }).build().display(message);
            }
        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            int[] count = {0};
            playlist.getTracks().forEach((track) -> {
                if (!bot.getBotSettings().isTooLong(track) && !track.equals(exclude)) {
                    AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                    audioHandler.addTrack(new QueuedTrack(track, event.getAuthor()));
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
                            bot.getBotSettings().getMaxTime()))).queue();
                } else {
                    message.editMessage(FormatUtils.filter(String.format(
                            "%1$s Found %2$s with `%3$s` entries; added to the queue! %4$s",
                            event.getClient().getSuccess(),
                            ((playlist.getName() == null) ? "a playlist" : "playlist **" + playlist.getName() + "**"),
                            playlist.getTracks().size(),
                            ((count < playlist.getTracks().size())
                                    ? String.format("\r\n%1$s Tracks longer than the allowed maximum (`%2$s`) have been omitted.",
                                    event.getClient().getWarning(),
                                    bot.getBotSettings().getMaxTime())
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
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), Constants.YT_SEARCH_PREFIX
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

    protected static class PlaylistCommand extends MusicCommand {
        PlaylistCommand(Bot bot) {
            super(bot);
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

            PlaylistLoader.Playlist playlist = bot.getPlaylistLoader().getPlaylist(event.getArgs());
            if (playlist == null) {
                event.replyError(String.format("I could not find `%1$s` in the Playlists folder.", event.getArgs()));
                return;
            }

            if (playlist.getItems().isEmpty() || (playlist.getItems() == null)) {
                event.replyWarning("Specified playlist is empty!");
            }

            event.getChannel().sendMessage(String.format("%1$s Loading playlist... **%2$s**... (%3$s items).",
                    bot.getBotSettings().getLoadingEmoji(), event.getArgs(), playlist.getItems().size())).queue(m ->
            {
                AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                playlist.loadTracks(bot.getPlayerManager(),
                        (audioTrack) -> audioHandler.addTrack(new QueuedTrack(audioTrack, event.getAuthor())), () -> {
                            StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                                    ? event.getClient().getWarning() + " No tracks were loaded!"
                                    : event.getClient().getSuccess()
                                    + " Loaded **" + playlist.getTracks().size() + "** tracks!");
                            if (!playlist.getErrors().isEmpty())
                                builder.append("\r\nThe following tracks failed to load:");
                            playlist.getErrors().forEach(
                                    err -> builder.append("\r\n`[").append(err.getIndex() + 1).append("]` **")
                                            .append(err.getItem()).append("**: ").append(err.getReason())
                            );
                            String str = builder.toString();
                            if (str.length() > 2000)
                                str = str.substring(0, 1994) + " (...)";
                            m.editMessage(FormatUtils.filter(str)).queue();
                        });
            });
        }
    }
}
