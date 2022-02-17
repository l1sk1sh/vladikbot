package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.l1sk1sh.vladikbot.data.entity.EmoteStatsRun;
import com.l1sk1sh.vladikbot.data.repository.EmoteStatsRunRepository;
import com.l1sk1sh.vladikbot.models.EmoteStatsRecord;
import com.l1sk1sh.vladikbot.services.EmoteStatsService;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.DateAndTimeUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class EmoteStatsCommand extends AdminCommand {

    @Autowired
    public EmoteStatsCommand(@Qualifier("backgroundThreadPool") ScheduledExecutorService backgroundThreadPool,
                             EventWaiter eventWaiter, EmoteStatsService emoteStatsService,
                             EmoteStatsRunRepository emoteStatsRunRepository) {
        this.name = "emotestats";
        this.help = "Calculates emotes statistics for current channel within selectable periods";
        this.guildOnly = true;
        this.children = new AdminCommand[]{
                new Total(backgroundThreadPool, emoteStatsService, eventWaiter),
                new LastTime(backgroundThreadPool, emoteStatsService, eventWaiter, emoteStatsRunRepository),
                new Ago(backgroundThreadPool, emoteStatsService, eventWaiter)
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(this, children, name).toString()).setEphemeral(true).queue();
    }

    private final class Total extends AdminCommand {

        private final ScheduledExecutorService backgroundThreadPool;
        private final EmoteStatsService emoteStatsService;
        private final EventWaiter eventWaiter;

        private Total(ScheduledExecutorService backgroundThreadPool,
                      EmoteStatsService emoteStatsService, EventWaiter eventWaiter) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.eventWaiter = eventWaiter;
            this.emoteStatsService = emoteStatsService;
            this.name = "total";
            this.help = "Calculate total statistics for current channel";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                event.deferReply(true).queue();
                List<EmoteStatsRecord> result
                        = emoteStatsService.getEmoteStatisticsByTotalUsageAmount(event.getChannel().getIdLong());

                if (result.isEmpty()) {
                    event.getHook().editOriginal(String.format("%1$s Result is empty!", getClient().getError())).queue();
                } else {
                    event.getHook().editOriginal(String.format("%1$s Sending result!", getClient().getSuccess())).queue();
                    sendStatisticsMessage(eventWaiter, event, result);
                }
            });
        }
    }

    private final class LastTime extends AdminCommand {

        private final ScheduledExecutorService backgroundThreadPool;
        private final EmoteStatsRunRepository emoteStatsRunRepository;
        private final EmoteStatsService emoteStatsService;
        private final EventWaiter eventWaiter;

        private LastTime(ScheduledExecutorService backgroundThreadPool, EmoteStatsService emoteStatsService,
                         EventWaiter eventWaiter, EmoteStatsRunRepository emoteStatsRunRepository) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.eventWaiter = eventWaiter;
            this.emoteStatsRunRepository = emoteStatsRunRepository;
            this.emoteStatsService = emoteStatsService;
            this.name = "last";
            this.help = "Calculate statistics for current channel since last emoji calculation time";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                event.deferReply(true).queue();
                EmoteStatsRun lastRun = emoteStatsRunRepository.getLastRunByChannelId(event.getChannel().getIdLong());
                long lastRunTime = 0L;
                if (lastRun != null) {
                    lastRunTime = lastRun.getLastLaunchedTime();
                }

                List<EmoteStatsRecord> result
                        = emoteStatsService.getEmoteStatisticsByTotalUsageAmountSince(event.getChannel().getIdLong(), lastRunTime);

                if (result.isEmpty()) {
                    event.getHook().editOriginal(String.format("%1$s Result is empty!", getClient().getError())).queue();
                } else {
                    event.getHook().editOriginal(String.format("%1$s Sending result!", getClient().getSuccess())).queue();
                    sendStatisticsMessage(eventWaiter, event, result);
                    emoteStatsRunRepository.save(new EmoteStatsRun(event.getChannel().getIdLong(), System.currentTimeMillis()));
                }
            });
        }
    }

    private final class Ago extends AdminCommand {

        private static final String AGO_OPTION_KEY = "ago";

        private final ScheduledExecutorService backgroundThreadPool;
        private final EmoteStatsService emoteStatsService;
        private final EventWaiter eventWaiter;

        private Ago(ScheduledExecutorService backgroundThreadPool,
                    EmoteStatsService emoteStatsService, EventWaiter eventWaiter) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.eventWaiter = eventWaiter;
            this.emoteStatsService = emoteStatsService;
            this.name = "since";
            this.help = "Calculate statistics for current channel for number of days ago";
            this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, AGO_OPTION_KEY, "Days ago").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                OptionMapping agoOption = event.getOption(AGO_OPTION_KEY);
                if (agoOption == null) {
                    event.replyFormat("%1$s Specify how many days ago should calculation start.", getClient().getWarning()).setEphemeral(true).queue();
                    return;
                }

                long agoTime = DateAndTimeUtils.getTimeNowMinusDays(agoOption.getAsLong());

                event.deferReply(true).queue();
                List<EmoteStatsRecord> result
                        = emoteStatsService.getEmoteStatisticsByTotalUsageAmountSince(event.getChannel().getIdLong(), agoTime);

                if (result.isEmpty()) {
                    event.getHook().editOriginal(String.format("%1$s Result is empty!", getClient().getError())).queue();
                } else {
                    event.getHook().editOriginal(String.format("%1$s Sending result!", getClient().getSuccess())).queue();
                    sendStatisticsMessage(eventWaiter, event, result);
                }
            });
        }
    }

    private void sendStatisticsMessage(EventWaiter eventWaiter, SlashCommandEvent event, List<EmoteStatsRecord> result) {
        int startPageNumber = 1;
        Paginator.Builder paginatorBuilder = new Paginator.Builder().setColumns(1)
                .setItemsPerPage(20)
                .showPageNumbers(true)
                .waitOnSinglePage(false)
                .useNumberedItems(false)
                .setFinalAction(m -> {
                    try {
                        m.clearReactions().queue();
                    } catch (PermissionException ex) {
                        m.delete().queue();
                    }
                })
                .setEventWaiter(eventWaiter)
                .setTimeout(1, TimeUnit.MINUTES);

        result = result.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());

        String[] printable = new String[result.size()];
        for (int i = 0; i < printable.length; i++) {
            EmoteStatsRecord record = result.get(i);
            printable[i] = getEmoteMentionByItsName(Objects.requireNonNull(event.getGuild()), record.getEmoteName())
                    + " #" + record.getAmount()
                    + " by " + Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getJDA().getUserById(record.getMostActiveUserId())).getAsTag();
        }

        paginatorBuilder.addItems(printable);

        Paginator paginator = paginatorBuilder
                .setColor(Color.black)
                .setText("Emote usage statistics for current channel:")
                .setUsers(event.getUser())
                .build();

        paginator.paginate(event.getChannel(), startPageNumber);
    }

    private String getEmoteMentionByItsName(Guild guild, String emoteName) {
        try {
            List<Emote> emojiIdList = guild.getEmotesByName(emoteName, false);

            if (!emojiIdList.isEmpty()) {
                return "<:" + emoteName + ":" + emojiIdList.get(0).getId() + ">";
            }
        } catch (IllegalArgumentException ignored) {
        }

        return emoteName;
    }
}
