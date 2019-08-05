package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.services.BackupChannelService;
import com.multiheaded.vladikbot.services.EmojiStatsService;
import com.multiheaded.vladikbot.settings.Constants;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.awt.*;
import java.io.IOException;
import java.security.InvalidParameterException;
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
        if (bot.isBackupAvailable()) {
            event.reply("Initializing emoji statistics calculation. Be patient...");

            new Thread(() -> {
                try {
                    EmojiStatsService emojiStatsService = new EmojiStatsService(
                            new BackupChannelService(
                                    event.getChannel().getId(),
                                    bot.getBotSettings().getToken(),
                                    Constants.BACKUP_PLAIN_TEXT,
                                    bot.getBotSettings().getLocalPathToExport(),
                                    bot.getBotSettings().getDockerPathToExport(),
                                    bot.getBotSettings().getDockerContainerName(),
                                    event.getArgs().split(" "),
                                    bot::setAvailableBackup
                            ).getExportedFile(),
                            event.getGuild().getEmotes(),
                            event.getArgs().split(" "),
                            bot::setAvailableBackup
                    );

                    if (emojiStatsService.getEmojiList() == null)
                        throw new RuntimeException("Emoji Statistics Service failed!");
                    sendStatisticsMessage(event, emojiStatsService.getEmojiList());
                } catch (InterruptedException | IOException e) {
                    event.replyError(String.format("Backup **has failed**! `[%1$s]`", e.getLocalizedMessage()));
                } catch (InvalidParameterException ipe) {
                    event.replyError(ipe.getLocalizedMessage());
                } catch (RuntimeException re) {
                    event.replyError(String.format("Calculation failed! `[%1$s]`", re.getLocalizedMessage()));
                } catch (Exception e) {
                    event.replyError(String.format("Crap! Whatever happened, it wasn't expected! `[%1$s]`", e.getLocalizedMessage()));
                }
            }).start();
        } else {
            event.replyWarning("Can't calculate emoji due to another backup in process!");
        }
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
