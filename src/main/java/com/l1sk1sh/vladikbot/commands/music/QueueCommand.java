package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
public class QueueCommand extends MusicCommand {
    private final Paginator.Builder builder;

    private final BotSettingsManager settings;
    private final NowPlayingHandler nowPlayingHandler;

    @Autowired
    public QueueCommand(EventWaiter eventWaiter, GuildSettingsRepository guildSettingsRepository,
                        PlayerManager playerManager, BotSettingsManager settings, NowPlayingHandler nowPlayingHandler) {
        super(guildSettingsRepository, playerManager);
        this.settings = settings;
        this.nowPlayingHandler = nowPlayingHandler;
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
                    } catch (PermissionException ignored) {
                    }
                })
                .setItemsPerPage(10)
                .waitOnSinglePage(false)
                .useNumberedItems(true)
                .showPageNumbers(true)
                .wrapPageEnds(true)
                .setEventWaiter(eventWaiter)
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    public void doCommand(CommandEvent event) {
        int pagenum = 1;
        try {
            pagenum = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException ignored) {
        }

        AudioHandler ah = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        List<QueuedTrack> list = Objects.requireNonNull(ah).getQueue().getList();
        if (list.isEmpty()) {
            Message nowp = ah.getNowPlaying(event.getJDA());
            Message nonowp = ah.getNoMusicPlaying(event.getJDA());
            Message built = new MessageBuilder()
                    .setContent(event.getClient().getWarning() + " There is no music in the queue!")
                    .setEmbed((nowp == null ? nonowp : nowp).getEmbeds().get(0)).build();
            event.reply(built, m ->
            {
                if (nowp != null)
                    nowPlayingHandler.setLastNPMessage(m);
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
                settings.get().isRepeat()))
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
                    ? Const.PAUSE_EMOJI : Const.PLAY_EMOJI).append(" **")
                    .append(audioPlayer.getPlayer().getPlayingTrack().getInfo().title).append("**\r\n");
        }

        return FormatUtils.filter(stringBuilder.append(success).append(" Current Queue | ").append(songslength)
                .append(" entries | `").append(FormatUtils.formatTimeTillHours(total)).append("` ")
                .append(repeatmode ? "| " + Const.REPEAT_EMOJI : "").toString());
    }
}
