package com.multiheaded.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.Paginator;
import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.services.audio.AudioHandler;
import com.multiheaded.vladikbot.models.queue.QueuedTrack;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class QueueCommand extends MusicCommand {
    private final Paginator.Builder builder;

    public QueueCommand(Bot bot) {
        super(bot);
        this.name = "queue";
        this.aliases = new String[]{"list"};
        this.help = "shows the current queue";
        this.arguments = "<pagenum>";
        this.bePlaying = true;
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
        builder = new Paginator.Builder()
                .setColumns(1)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ignore) {
                    }
                })
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(bot.getWaiter())
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    public void doCommand(CommandEvent event) {
        int pagenum = 1;
        try {
            pagenum = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException ignore) {
        }

        AudioHandler ah = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        List<QueuedTrack> list = ah.getQueue().getList();
        if (list.isEmpty()) {
            Message nowp = ah.getNowPlaying(event.getJDA());
            Message nonowp = ah.getNoMusicPlaying(event.getJDA());
            Message built = new MessageBuilder()
                    .setContent(event.getClient().getWarning() + " There is no music in the queue!")
                    .setEmbed((nowp == null ? nonowp : nowp).getEmbeds().get(0)).build();
            event.reply(built, m ->
            {
                if (nowp != null)
                    bot.getNowPlayingHandler().setLastNPMessage(m);
            });
            return;
        }

        String[] songs = new String[list.size()];
        long total = 0;
        for (int i = 0; i < list.size(); i++) {
            total += list.get(i).getTrack().getDuration();
            songs[i] = list.get(i).toString();
        }

        long fintotal = total;
        builder.setText((i1, i2) -> getQueueTitle(ah, event.getClient().getSuccess(), songs.length, fintotal,
                bot.getBotSettings().shouldRepeat()))
                .setItems(songs)
                .setUsers(event.getAuthor())
                .setColor(event.getSelfMember().getColor())
        ;
        builder.build().paginate(event.getChannel(), pagenum);
    }

    private String getQueueTitle(AudioHandler audioPlayer, String success, int songslength, long total, boolean repeatmode) {
        StringBuilder stringBuilder = new StringBuilder();
        if (audioPlayer.getPlayer().getPlayingTrack() != null) {
            stringBuilder.append(audioPlayer.getPlayer().isPaused()
                    ? Constants.PAUSE_EMOJI : Constants.PLAY_EMOJI).append(" **")
                    .append(audioPlayer.getPlayer().getPlayingTrack().getInfo().title).append("**\r\n");
        }

        return FormatUtils.filter(stringBuilder.append(success).append(" Current Queue | ").append(songslength)
                .append(" entries | `").append(FormatUtils.formatTime(total)).append("` ")
                .append(repeatmode ? "| " + Constants.REPEAT_EMOJI : "").toString());
    }
}
