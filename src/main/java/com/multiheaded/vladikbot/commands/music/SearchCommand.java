package com.multiheaded.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.services.audio.AudioHandler;
import com.multiheaded.vladikbot.models.queue.QueuedTrack;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SearchCommand extends MusicCommand {
    private final OrderedMenu.Builder builder;
    String searchPrefix;

    public SearchCommand(Bot bot) {
        super(bot);
        this.name = "search";
        this.arguments = "<query>";
        this.help = "searches Youtube for a provided query";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.searchPrefix = Constants.YT_SEARCH_PREFIX;
        builder = new OrderedMenu.Builder()
                .allowTextInput(true)
                .useNumbers()
                .useCancelButton(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a query.");
            return;
        }

        event.reply(String.format("%1$s  Searching... `[%2$s]`", bot.getBotSettings().getSearchingEmoji(), event.getArgs()),
                m -> bot.getPlayerManager().loadItemOrdered(
                        event.getGuild(), searchPrefix + event.getArgs(), new ResultHandler(m, event)));
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message message;
        private final CommandEvent event;

        private ResultHandler(Message message, CommandEvent event) {
            this.message = message;
            this.event = event;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
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
            message.editMessage(FormatUtils.filter(String.format(
                    "%1$s Added **%2$s** (`%3$s`) %4$s.",
                    event.getClient().getSuccess(),
                    track.getInfo().title,
                    FormatUtils.formatTime(track.getDuration()),
                    ((pos == 0) ? "to begin playing" : " to the queue at position " + pos)))
            ).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            builder.setColor(event.getSelfMember().getColor())
                    .setText(FormatUtils.filter(event.getClient().getSuccess()
                            + " Search results for `" + event.getArgs() + "`:"))
                    .setChoices()
                    .setSelection((msg, i) ->
                    {
                        AudioTrack track = playlist.getTracks().get(i - 1);
                        if (bot.getBotSettings().isTooLong(track)) {
                            event.replyWarning(String.format(
                                    "This track (**%1$s**) is longer than the allowed maximum: `%2$s` > `%3$s`.",
                                    track.getInfo().title,
                                    FormatUtils.formatTime(track.getDuration()),
                                    bot.getBotSettings().getMaxTime()));
                            return;
                        }
                        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                        int pos = audioHandler.addTrack(new QueuedTrack(track, event.getAuthor())) + 1;
                        event.replySuccess(String.format(
                                "Added **%1$s** (`%2$s`) %3$s.",
                                track.getInfo().title,
                                FormatUtils.formatTime(track.getDuration()),
                                ((pos == 0) ? "to begin playing" : " to the queue at position " + pos))
                        );
                    })
                    .setCancel((msg) -> {
                    })
                    .setUsers(event.getAuthor())
            ;
            for (int i = 0; i < 4 && i < playlist.getTracks().size(); i++) {
                AudioTrack track = playlist.getTracks().get(i);
                builder.addChoices("`[" + FormatUtils.formatTime(track.getDuration()) + "]` [**"
                        + track.getInfo().title + "**](" + track.getInfo().uri + ")");
            }
            builder.build().display(message);
        }

        @Override
        public void noMatches() {
            message.editMessage(FormatUtils.filter(String.format(
                    "%1$s No results found for `%2$s`.", event.getClient().getWarning(), event.getArgs()))).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON) {
                message.editMessage(String.format("%1$s Error loading: %2$s.",
                        event.getClient().getError(), throwable.getLocalizedMessage())).queue();
            } else {
                message.editMessage(String.format("%1$s Error loading track.", event.getClient().getError())).queue();
            }
        }
    }
}
