package com.multiheaded.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.audio.AudioHandler;
import com.multiheaded.vladikbot.audio.QueuedTrack;
import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;
import com.multiheaded.vladikbot.utils.FormatUtil;
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
    private final Settings settings;
    String searchPrefix = "ytsearch:";

    public SearchCommand(VladikBot bot) {
        super(bot);
        this.name = "search";
        this.aliases = new String[]{"ytsearch"};
        this.arguments = "<query>";
        this.help = "searches Youtube for a provided query";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        builder = new OrderedMenu.Builder()
                .allowTextInput(true)
                .useNumbers()
                .useCancelButton(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);

        settings = SettingsManager.getInstance().getSettings();
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a query.");
            return;
        }

        event.reply(settings.getSearchingEmoji() + " Searching... `[" + event.getArgs() + "]`",
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
            if (settings.isTooLong(track)) {
                message.editMessage(FormatUtil.filter(event.getClient().getWarning()
                        + " This track (**" + track.getInfo().title + "**) is longer than the allowed maximum: `"
                        + FormatUtil.formatTime(track.getDuration()) + "` > `"
                        + settings.getMaxTime() + "`")).queue();
                return;
            }
            AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = audioHandler.addTrack(new QueuedTrack(track, event.getAuthor())) + 1;
            message.editMessage(FormatUtil.filter(event.getClient().getSuccess()
                    + " Added **" + track.getInfo().title
                    + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "to begin playing"
                    : " to the queue at position " + pos))).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            builder.setColor(event.getSelfMember().getColor())
                    .setText(FormatUtil.filter(event.getClient().getSuccess()
                            + " Search results for `" + event.getArgs() + "`:"))
                    .setChoices()
                    .setSelection((msg, i) ->
                    {
                        AudioTrack track = playlist.getTracks().get(i - 1);
                        if (settings.isTooLong(track)) {
                            event.replyWarning("This track (**" + track.getInfo().title
                                    + "**) is longer than the allowed maximum: `"
                                    + FormatUtil.formatTime(track.getDuration()) + "` > `"
                                    + settings.getMaxTime() + "`");
                            return;
                        }
                        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                        int pos = audioHandler.addTrack(new QueuedTrack(track, event.getAuthor())) + 1;
                        event.replySuccess("Added **" + track.getInfo().title
                                + "** (`" + FormatUtil.formatTime(track.getDuration()) + "`) "
                                + (pos == 0 ? "to begin playing"
                                : " to the queue at position " + pos));
                    })
                    .setCancel((msg) -> {
                    })
                    .setUsers(event.getAuthor())
            ;
            for (int i = 0; i < 4 && i < playlist.getTracks().size(); i++) {
                AudioTrack track = playlist.getTracks().get(i);
                builder.addChoices("`[" + FormatUtil.formatTime(track.getDuration()) + "]` [**"
                        + track.getInfo().title + "**](" + track.getInfo().uri + ")");
            }
            builder.build().display(message);
        }

        @Override
        public void noMatches() {
            message.editMessage(FormatUtil.filter(event.getClient().getWarning() + " No results found for `"
                    + event.getArgs() + "`.")).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON) {
                message.editMessage(event.getClient().getError()
                        + " Error loading: " + throwable.getMessage()).queue();
            } else {
                message.editMessage(event.getClient().getError()
                        + " Error loading track.").queue();
            }
        }
    }
}
