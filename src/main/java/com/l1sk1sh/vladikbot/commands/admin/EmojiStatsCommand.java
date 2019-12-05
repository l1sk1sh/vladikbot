package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.services.BackupTextChannelService;
import com.l1sk1sh.vladikbot.services.EmojiStatsService;
import com.l1sk1sh.vladikbot.settings.Const;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toMap;

/**
 * @author Oliver Johnson
 */
public class EmojiStatsCommand extends AdminCommand {
    private final Paginator.Builder pbuilder;
    private final Bot bot;

    public EmojiStatsCommand(EventWaiter waiter, Bot bot) {
        this.bot = bot;
        this.name = "emojistats";
        this.help = "returns full or partial statistics **(once in 24h)** of emoji usage in the current channel\r\n"
                + "\t\t `-b, --before <mm/dd/yyyy>` - specifies date till which statics would be done\r\n"
                + "\t\t `-a, --after  <mm/dd/yyyy>` - specifies date from which statics would be done\r\n"
                + "\t\t `-iu` - ignores unicode emoji and unknown emoji\r\n"
                + "\t\t `-f` - creates new backup despite existing one\r\n"
                + "\t\t `-i` - ignores unicode emoji.";
        this.arguments = "-a, -b, -iu, -i, -f";
        this.guildOnly = true;

        pbuilder = new Paginator.Builder().setColumns(1)
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
                .setEventWaiter(waiter)
                .setTimeout(1, TimeUnit.MINUTES);
    }

    @Override
    public void execute(CommandEvent event) {
        if (bot.isLockedBackup()) {
            event.replyWarning("Can't calculate emoji due to another backup in process!");
            return;
        }
        event.reply("Initializing emoji statistics calculation. Be patient...");

        BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                bot,
                event.getChannel().getId(),
                Const.BACKUP_PLAIN_TEXT,
                bot.getBotSettings().getLocalTmpFolder(),
                event.getArgs().split(" ")
        );

        /* This thread will wait for backup to finish. Separating allows using bot while backup is running */
        new Thread(() -> {

            /* Creating new thread from text backup service and waiting for it to finish */
            Thread backupChannelServiceThread = new Thread(backupTextChannelService);
            backupChannelServiceThread.start();
            try {
                backupChannelServiceThread.join();
            } catch (InterruptedException e) {
                event.replyError("Backup process was interrupted!");
                return;
            }

            if (backupTextChannelService.hasFailed()) {
                event.replyError(String.format("Text channel backup has failed: `[%s]`", backupTextChannelService.getFailMessage()));
                return;
            }

            File exportedTextFile = backupTextChannelService.getBackupFile();

            /* Creating new thread from text backup service and waiting for it to finish */
            EmojiStatsService emojiStatsService = new EmojiStatsService(
                    bot,
                    exportedTextFile,
                    event.getGuild().getEmotes(),
                    event.getArgs().split(" ")
            );

            Thread emojiStatsServiceThread = new Thread(emojiStatsService);
            emojiStatsServiceThread.start();
            try {
                emojiStatsServiceThread.join();
            } catch (InterruptedException e) {
                event.replyError("Emoji Service was interrupted!");
                return;
            }

            if (emojiStatsService.hasFailed()) {
                event.replyError(String.format("Emoji Statistics Service has failed: `[%s]`", emojiStatsService.getFailMessage()));
                return;
            }
            sendStatisticsMessage(event, emojiStatsService.getEmojiList());

        }).start();
    }

    private void sendStatisticsMessage(CommandEvent event, Map<String, Integer> emojiMap) {
        int startPageNumber = 1;
        pbuilder.clearItems();

        Map<String, Integer> preparedEmojiMap = new HashMap<>(emojiMap);

        /* Prepare guild's emojis for displaying <:emoji:id> */
        for (Map.Entry<String, Integer> entry : emojiMap.entrySet()) {
            if (entry.getKey().contains(":")) {
                String emojiName = entry.getKey().replaceAll(":", "");
                try {
                    List<Emote> emojiIdList = event.getGuild().getEmotesByName(emojiName, true);

                    if (emojiIdList.size() != 0) {
                        preparedEmojiMap.put("<:" + emojiName + ":" + emojiIdList.get(0).getId() + ">",
                                preparedEmojiMap.remove(entry.getKey()));
                    }
                } catch (IllegalArgumentException emptyName) { /* Ignore */
                }
            }
        }

        /* Sort Descending using Stream API */
        preparedEmojiMap = preparedEmojiMap
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        /* Form String[] for PageBuilder addItems(String... input) method */
        String[] keys = preparedEmojiMap.keySet().toArray(new String[0]);
        String[] values = Arrays.stream(preparedEmojiMap.values().toArray(new Integer[0]))
                .map(String::valueOf)
                .toArray(String[]::new);
        String[] resultSet = new String[preparedEmojiMap.size()];

        for (int i = 0; i < preparedEmojiMap.size(); i++) {
            resultSet[i] = keys[i] + "=" + values[i];
        }

        pbuilder.addItems(resultSet);

        Paginator paginator = pbuilder
                .setColor(event.isFromType(ChannelType.TEXT) ? event.getSelfMember().getColor() : Color.black)
                .setText("Emoji usage statistics for current channel:")
                .setUsers(event.getAuthor())
                .build();

        paginator.paginate(event.getChannel(), startPageNumber);
    }
}
