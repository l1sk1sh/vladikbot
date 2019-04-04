package com.multiheaded.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.audio.AudioHandler;
import com.multiheaded.vladikbot.audio.QueuedTrack;
import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;
import com.multiheaded.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class PlayNextCommand extends DJCommand {
    private final Settings settings;

    public PlayNextCommand(VladikBot bot) {
        super(bot);
        this.name = "playnext";
        this.arguments = "<title|URL>";
        this.help = "plays a single song next";
        this.beListening = true;
        this.bePlaying = false;
        settings = SettingsManager.getInstance().getSettings();
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            event.replyWarning("Please include a song title or URL!");
            return;
        }
        String args = event.getArgs().startsWith("<") && event.getArgs().endsWith(">")
                ? event.getArgs().substring(1, event.getArgs().length() - 1)
                : event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs();
        event.reply(settings.getLoadingEmoji() + " Loading... `[" + args + "]`", m -> bot.getPlayerManager()
                .loadItemOrdered(event.getGuild(), args, new ResultHandler(m, event, false)));
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

        private void loadSingle(AudioTrack track) {
            if (settings.isTooLong(track)) {
                message.editMessage(FormatUtils.filter(event.getClient().getWarning()
                        + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `"
                        + FormatUtils.formatTime(track.getDuration())
                        + "` > `" + FormatUtils.formatTime(settings.getMaxSeconds() * 1000) + "`")).queue();
                return;
            }
            AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = audioHandler.addTrackToFront(new QueuedTrack(track, event.getAuthor())) + 1;
            String addMessage = FormatUtils.filter(event.getClient().getSuccess()
                    + " Added **" + track.getInfo().title
                    + "** (`" + FormatUtils.formatTime(track.getDuration()) + "`) "
                    + (pos == 0 ? "to begin playing" : " to the queue at position " + pos));
            message.editMessage(addMessage).queue();
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            AudioTrack single;
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                single = playlist.getSelectedTrack()
                        == null ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
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
                message.editMessage(FormatUtils.filter(event.getClient().getWarning()
                        + " No results found for `" + event.getArgs() + "`.")).queue();
            } else {
                bot.getPlayerManager().loadItemOrdered(event.getGuild(),
                        "ytsearch:" + event.getArgs(), new ResultHandler(message, event, true));
            }
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == FriendlyException.Severity.COMMON) {
                message.editMessage(event.getClient().getError()
                        + " Error loading: " + throwable.getMessage()).queue();
            } else {
                message.editMessage(event.getClient().getError()
                        + " Error loading track.").queue();
            }
        }
    }
}
