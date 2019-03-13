package com.multiheaded.disbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Paginator;
import com.multiheaded.disbot.models.conductors.EmojiStatsConductor;
import com.sun.org.apache.xalan.internal.xsltc.runtime.InternalRuntimeError;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.awt.*;
import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toMap;

/**
 * @author Oliver Johnson
 */
public class EmojiStatsCommand extends Command {
    private final Paginator.Builder pbuilder;

    public EmojiStatsCommand(EventWaiter waiter) {
        this.name = "emojistats";
        this.help = "returns full or partial statistics **(once in 24h)** of emoji usage in the current channel\n"
                + "\t\t `-b, --before <mm/dd/yyyy>` - specifies date till which statics would be done.\n"
                + "\t\t `-a, --after  <mm/dd/yyyy>` - specifies date from which statics would be done.\n"
                + "\t\t `-iu` - ignores unicode emoji and unknown emoji.\n"
                + "\t\t `-f` - creates new backup despite existing one.\n"
                + "\t\t `-i` - ignores unicode emoji.";
        this.arguments = "-a, -b, -iu, -i, -f";
        this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY,
                Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS};
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
        event.reply("Initializing emoji statistics calculation. Be patient...");

        new Thread(() -> {
            try {
                EmojiStatsConductor emojiStatsConductor =
                        new EmojiStatsConductor(event.getChannel().getId(),
                                event.getArgs().split(" "), event.getGuild().getEmotes());

                if (emojiStatsConductor.getEmojiStatsService().getEmojiList() == null)
                    throw new InternalRuntimeError("Emoji Statistics Service failed!");
                sendStatisticsMessage(event, emojiStatsConductor.getEmojiStatsService().getEmojiList());
            } catch (InterruptedException | FileNotFoundException e) {
                event.replyError(String.format("Backup **has failed**! `[%s]`", e.getMessage()));
            } catch (InvalidParameterException ipe) {
                event.replyError(ipe.getMessage());
            } catch (InternalRuntimeError ire) {
                event.replyError(String.format("Calculation failed! `[%s]`", ire.getMessage()));
            }
        }).start();
    }

    private void sendStatisticsMessage(CommandEvent event, Map<String, Integer> emojiMap) {
        int startPageNumber = 1;

        Map<String, Integer> preparedEmojiMap = new HashMap<>(emojiMap);

        // Prepare server emojis for displaying <:emoji:id>
        for (Map.Entry<String, Integer> entry : emojiMap.entrySet()) {
            if (entry.getKey().contains(":")) {
                String emojiName = entry.getKey().replaceAll(":", "");
                List<Emote> emojiIdList = event.getGuild().getEmotesByName(emojiName, true);

                if (emojiIdList.size() != 0) {
                    preparedEmojiMap.put("<:" + emojiName + ":" + emojiIdList.get(0).getId() + ">",
                            preparedEmojiMap.remove(entry.getKey()));
                }
            }
        }

        // Sort Descending using Stream API
        preparedEmojiMap = preparedEmojiMap
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        // Form String[] for PageBuilder addItems(String... input) method
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
