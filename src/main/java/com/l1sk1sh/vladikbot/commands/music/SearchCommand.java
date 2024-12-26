package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
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
 * @author John Grosh
 */
@Slf4j
@Service
public class SearchCommand extends MusicCommand {

    private static final String QUERY_OPTION_KEY = "query";

    private final BotSettingsManager settings;
    private final PlayerManager playerManager;
    private final OrderedMenu.Builder builder;
    protected String searchPrefix;

    @Autowired
    public SearchCommand(EventWaiter eventWaiter, GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager, BotSettingsManager settings) {
        super(guildSettingsRepository, playerManager);
        this.settings = settings;
        this.playerManager = playerManager;
        this.name = "msearch";
        this.help = "Searches Youtube for a provided query";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, QUERY_OPTION_KEY, "Something to be searched in youtube").setRequired(true));
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.searchPrefix = Const.YT_SEARCH_PREFIX;
        builder = new OrderedMenu.Builder()
                .allowTextInput(true)
                .useNumbers()
                .useCancelButton(true)
                .setEventWaiter(eventWaiter)
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        OptionMapping searchOption = event.getOption(QUERY_OPTION_KEY);
        if (searchOption == null) {
            event.replyFormat("%1$s Please include a search query.", event.getClient().getWarning()).setEphemeral(true).queue();

            return;
        }
        String query = searchOption.getAsString();

        event.deferReply(true).setEphemeral(false).queue();

        playerManager.loadItemOrdered(event.getGuild(), searchPrefix + query, new ResultHandler(query, event));
    }

    private final class ResultHandler implements AudioLoadResultHandler {
        private final String query;
        private final SlashCommandEvent event;

        private ResultHandler(String query, SlashCommandEvent event) {
            this.query = query;
            this.event = event;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            if (settings.get().isTooLong(track)) {
                event.getHook().editOriginal(
                        String.format("%1$s This track (**%2$s**) is longer than the allowed maximum: `%3$s` > `%4$s`.",
                                event.getClient().getWarning(),
                                FormatUtils.filter(track.getInfo().title),
                                FormatUtils.formatTimeTillHours(track.getDuration()),
                                FormatUtils.formatTimeTillHours(settings.get().getMaxSeconds() * 1000)
                        )).queue();

                return;
            }

            AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
            int position = Objects.requireNonNull(audioHandler).addTrack(new QueuedTrack(track, event.getUser())) + 1;

            event.getHook().editOriginal(
                    String.format("%1$s Added **%2$s** (`%3$s`) %4$s.",
                            event.getClient().getSuccess(),
                            FormatUtils.filter(track.getInfo().title),
                            FormatUtils.formatTimeTillHours(track.getDuration()),
                            ((position == 0) ? "to begin playing" : " to the queue at position " + position)
                    )).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            builder.setColor(Objects.requireNonNull(event.getGuild()).getSelfMember().getColor())
                    .setText(FormatUtils.filter(event.getClient().getSuccess()
                            + " Search results for `" + FormatUtils.filter(query) + "`:"))
                    .setChoices()
                    .setSelection((msg, i) ->
                    {
                        AudioTrack track = playlist.getTracks().get(i - 1);
                        if (settings.get().isTooLong(track)) {
                            event.getHook().editOriginalFormat(
                                    "%1$s This track (**%2$s**) is longer than the allowed maximum: `%3$s` > `%4$s`.",
                                    event.getClient().getWarning(),
                                    track.getInfo().title,
                                    FormatUtils.formatTimeTillHours(track.getDuration()),
                                    settings.get().getMaxTime()
                            ).queue();

                            return;
                        }

                        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                        int position = Objects.requireNonNull(audioHandler).addTrack(new QueuedTrack(track, event.getUser())) + 1;

                        /* Sending new message to the channel as old one is removed by OrderedMenu */
                        event.getTextChannel().sendMessageFormat(
                                "%1$s Added **%2$s** (`%3$s`) %4$s.",
                                event.getClient().getSuccess(),
                                FormatUtils.filter(track.getInfo().title),
                                FormatUtils.formatTimeTillHours(track.getDuration()),
                                ((position == 0) ? "to begin playing" : " to the queue at position " + position)
                        ).complete();
                    })
                    .setCancel((msg) -> {
                    })
                    .setUsers(event.getUser())
            ;

            for (int i = 0; i < 4 && i < playlist.getTracks().size(); i++) {
                AudioTrack track = playlist.getTracks().get(i);
                builder.addChoices("`[" + FormatUtils.formatTimeTillHours(track.getDuration()) + "]` [**"
                        + track.getInfo().title + "**](" + track.getInfo().uri + ")");
            }

            Message message = event.getHook().retrieveOriginal().complete();
            event.getHook().editOriginalFormat(Const.LOADING_SYMBOL).complete(); // Required to remove "is thinking"
            builder.build().display(message);
        }

        @Override
        public void noMatches() {
            event.getHook().editOriginalFormat("%1$s No results found for `%2$s`.", event.getClient().getWarning(), FormatUtils.filter(query)).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON) {
                event.getHook().editOriginalFormat("%1$s Error loading: %2$s.",
                        event.getClient().getError(), throwable.getLocalizedMessage()).queue();
            } else {
                event.getHook().editOriginalFormat("%1$s Error loading track.",
                        event.getClient().getError()).queue();
            }
        }
    }
}
