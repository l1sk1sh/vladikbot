package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.l1sk1sh.vladikbot.data.entity.EmojiStatsExecution;
import com.l1sk1sh.vladikbot.data.repository.EmojiStatsRunRepository;
import com.l1sk1sh.vladikbot.models.EmojiStatsRecord;
import com.l1sk1sh.vladikbot.services.EmojiStatsService;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.DateAndTimeUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class EmojiStatsCommand extends AdminCommand {

    @Autowired
    public EmojiStatsCommand(@Qualifier("backgroundThreadPool") ScheduledExecutorService backgroundThreadPool,
                             EventWaiter eventWaiter, EmojiStatsService emojiStatsService,
                             EmojiStatsRunRepository emojiStatsRunRepository) {
        this.name = "emojistats";
        this.help = "Calculates emojis statistics for current channel within selectable periods";
        this.guildOnly = true;
        this.children = new AdminCommand[]{
                new Total(backgroundThreadPool, emojiStatsService, eventWaiter),
                new LastTime(backgroundThreadPool, emojiStatsService, eventWaiter, emojiStatsRunRepository),
                new Ago(backgroundThreadPool, emojiStatsService, eventWaiter)
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString()).setEphemeral(true).queue();
    }

    private final class Total extends AdminCommand {

        private final ScheduledExecutorService backgroundThreadPool;
        private final EmojiStatsService emojiStatsService;
        private final EventWaiter eventWaiter;

        private Total(ScheduledExecutorService backgroundThreadPool,
                      EmojiStatsService emojiStatsService, EventWaiter eventWaiter) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.eventWaiter = eventWaiter;
            this.emojiStatsService = emojiStatsService;
            this.name = "total";
            this.help = "Calculate total statistics for current channel";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                event.deferReply(true).queue();
                List<EmojiStatsRecord> result
                        = emojiStatsService.getEmojiStatisticsByTotalUsageAmount(event.getChannel().getIdLong());

                if (result.isEmpty()) {
                    event.getHook().editOriginal(String.format("%1$s Result is empty!", event.getClient().getError())).queue();
                } else {
                    event.getHook().editOriginal(String.format("%1$s Sending result!", event.getClient().getSuccess())).queue();
                    sendStatisticsMessage(eventWaiter, event, result);
                }
            });
        }
    }

    private final class LastTime extends AdminCommand {

        private final ScheduledExecutorService backgroundThreadPool;
        private final EmojiStatsRunRepository emojiStatsRunRepository;
        private final EmojiStatsService emojiStatsService;
        private final EventWaiter eventWaiter;

        private LastTime(ScheduledExecutorService backgroundThreadPool, EmojiStatsService emojiStatsService,
                         EventWaiter eventWaiter, EmojiStatsRunRepository emojiStatsRunRepository) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.eventWaiter = eventWaiter;
            this.emojiStatsRunRepository = emojiStatsRunRepository;
            this.emojiStatsService = emojiStatsService;
            this.name = "last";
            this.help = "Calculate statistics for current channel since last emoji calculation time";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                event.deferReply(true).queue();
                EmojiStatsExecution lastRun = emojiStatsRunRepository.getLastRunByChannelId(event.getChannel().getIdLong());
                long lastRunTime = 0L;
                if (lastRun != null) {
                    lastRunTime = lastRun.getLastLaunchedTime();
                }

                List<EmojiStatsRecord> result
                        = emojiStatsService.getEmojiStatisticsByTotalUsageAmountSince(event.getChannel().getIdLong(), lastRunTime);

                if (result.isEmpty()) {
                    event.getHook().editOriginal(String.format("%1$s Result is empty!", event.getClient().getError())).queue();
                } else {
                    event.getHook().editOriginal(String.format("%1$s Sending result!", event.getClient().getSuccess())).queue();
                    sendStatisticsMessage(eventWaiter, event, result);
                    emojiStatsRunRepository.save(new EmojiStatsExecution(event.getChannel().getIdLong(), System.currentTimeMillis()));
                }
            });
        }
    }

    private final class Ago extends AdminCommand {

        private static final String AGO_OPTION_KEY = "ago";

        private final ScheduledExecutorService backgroundThreadPool;
        private final EmojiStatsService emojiStatsService;
        private final EventWaiter eventWaiter;

        private Ago(ScheduledExecutorService backgroundThreadPool,
                    EmojiStatsService emojiStatsService, EventWaiter eventWaiter) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.eventWaiter = eventWaiter;
            this.emojiStatsService = emojiStatsService;
            this.name = "since";
            this.help = "Calculate statistics for current channel for number of days ago";
            this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, AGO_OPTION_KEY, "Days ago").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                OptionMapping agoOption = event.getOption(AGO_OPTION_KEY);
                if (agoOption == null) {
                    event.replyFormat("%1$s Specify how many days ago should calculation start.", event.getClient().getWarning()).setEphemeral(true).queue();
                    return;
                }

                long agoTime = DateAndTimeUtils.getEpochMillisNowMinusDays(agoOption.getAsLong());

                event.deferReply(true).queue();
                List<EmojiStatsRecord> result
                        = emojiStatsService.getEmojiStatisticsByTotalUsageAmountSince(event.getChannel().getIdLong(), agoTime);

                if (result.isEmpty()) {
                    event.getHook().editOriginal(String.format("%1$s Result is empty!", event.getClient().getError())).queue();
                } else {
                    event.getHook().editOriginal(String.format("%1$s Sending result!", event.getClient().getSuccess())).queue();
                    sendStatisticsMessage(eventWaiter, event, result);
                }
            });
        }
    }

    private void sendStatisticsMessage(EventWaiter eventWaiter, SlashCommandEvent event, List<EmojiStatsRecord> result) {
        int startPageNumber = 1;
        Paginator.Builder paginatorBuilder = new Paginator.Builder().setColumns(1)
                .setItemsPerPage(35)
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
                .setTimeout(5, TimeUnit.MINUTES);

        result = result.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());

        String[] printable = new String[result.size()];
        for (int i = 0; i < printable.length; i++) {
            EmojiStatsRecord record = result.get(i);
            printable[i] = getEmojiMentionByItsName(event.getGuild(), record.getEmojiName())
                    + " #" + record.getAmount()
                    + " by " + getAuthor(event.getGuild(), record.getMostActiveUserId());
        }

        paginatorBuilder.addItems(printable);

        Paginator paginator = paginatorBuilder
                .setColor(Color.black)
                .setText("Emoji usage statistics for current channel:")
                .setUsers(event.getUser())
                .build();

        paginator.paginate(event.getChannel(), startPageNumber);
    }

    private String getAuthor(Guild guild, long userId) {
        if (guild == null) {
            return "__unknown__";
        }

        User user = guild.getJDA().getUserById(userId);
        if (user == null) {
            return "__unknown__";
        }

        return "**" + user.getName() + "**";
    }

    private String getEmojiMentionByItsName(Guild guild, String emojiName) {
        if (guild == null) {
            return emojiName;
        }

        try {
            List<RichCustomEmoji> emojiIdList = guild.getEmojisByName(emojiName, false);

            if (!emojiIdList.isEmpty()) {
                return "<:" + emojiName + ":" + emojiIdList.get(0).getId() + ">";
            }
        } catch (IllegalArgumentException ignored) {
        }

        return emojiName;
    }
}
