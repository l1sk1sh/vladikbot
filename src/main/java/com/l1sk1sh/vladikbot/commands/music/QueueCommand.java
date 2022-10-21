package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.AudioRepeatMode;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public class QueueCommand extends MusicCommand {
    private final Paginator.Builder builder;

    private static final String PAGE_NUM_OPTION_KEY = "pagenum";

    private final BotSettingsManager settings;
    private final NowPlayingHandler nowPlayingHandler;

    @Autowired
    public QueueCommand(EventWaiter eventWaiter, GuildSettingsRepository guildSettingsRepository,
                        PlayerManager playerManager, BotSettingsManager settings, NowPlayingHandler nowPlayingHandler) {
        super(guildSettingsRepository, playerManager);
        this.settings = settings;
        this.nowPlayingHandler = nowPlayingHandler;
        this.name = "mqueue";
        this.help = "Shows the current music queue";
        this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, PAGE_NUM_OPTION_KEY, "Switch to selected page of the queue").setRequired(false));
        this.botPermissions = new Permission[]{Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EMBED_LINKS};
        this.bePlaying = true;
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
                .setTimeout(2, TimeUnit.MINUTES);
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        int pagenum = 1;
        OptionMapping pagenumOption = event.getOption(PAGE_NUM_OPTION_KEY);
        if (pagenumOption != null) {
            pagenum = (int) pagenumOption.getAsLong();
        }

        AudioHandler ah = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        List<QueuedTrack> list = Objects.requireNonNull(ah).getQueue().getList();
        if (list.isEmpty()) {
            MessageCreateData nowp = ah.getNowPlaying(event.getJDA());
            MessageCreateData nonowp = ah.getNoMusicPlaying(event.getJDA());
            MessageCreateData built = new MessageCreateBuilder()
                    .setContent(event.getClient().getWarning() + " There is no music in the queue!")
                    .setEmbeds((nowp == null ? nonowp : nowp).getEmbeds().get(0)).build();

            event.reply(built).queue();

            if (nowp != null) {
                nowPlayingHandler.setLastNPMessage(event.getHook().retrieveOriginal().complete());
            }

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
                settings.get().getRepeat()))
                .setItems(songs)
                .setUsers(event.getUser())
                .setColor(event.getGuild().getSelfMember().getColor())
        ;

        event.replyFormat("%1$s Loading...", settings.get().getLoadingEmoji()).queue();
        builder.build().paginate(event.getHook().retrieveOriginal().complete(), pagenum);
    }

    private String getQueueTitle(AudioHandler audioPlayer, String success, int songslength, long total, AudioRepeatMode repeatmode) {
        StringBuilder stringBuilder = new StringBuilder();
        if (audioPlayer.getPlayer().getPlayingTrack() != null) {
            stringBuilder.append(audioPlayer.getPlayer().isPaused()
                    ? Const.PAUSE_EMOJI : Const.PLAY_EMOJI).append(" **")
                    .append(audioPlayer.getPlayer().getPlayingTrack().getInfo().title).append("**\r\n");
        }

        return FormatUtils.filter(stringBuilder.append(success).append(" Current Queue | ").append(songslength)
                .append(" entries | `").append(FormatUtils.formatTimeTillHours(total)).append("` ")
                .append(repeatmode.getEmoji() != null ? "| " + repeatmode.getEmoji() : "").toString());
    }
}
