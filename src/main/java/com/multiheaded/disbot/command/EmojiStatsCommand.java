package com.multiheaded.disbot.command;

import com.multiheaded.disbot.core.EmojiStatsConductor;
import com.sun.org.apache.xalan.internal.xsltc.runtime.InternalRuntimeError;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.*;

import static com.multiheaded.disbot.settings.Constants.BOT_PREFIX;

public class EmojiStatsCommand extends AbstractCommand {

    @Override
    public void onCommand(MessageReceivedEvent event, String[] args) {

        if (!event.isFromType(ChannelType.PRIVATE)) {
            sendMessage(event, "Initializing emoji statistics calculation. Be patient...");

            new Thread(() -> {
                try {
                    EmojiStatsConductor emojiStatsConductor =
                            new EmojiStatsConductor(event.getChannel().getId(), args, event.getGuild().getEmotes());

                    if (emojiStatsConductor.getEmojiStatsService().getEmojiList() == null)
                        throw new InternalRuntimeError("Emoji Statistics Service failed!");
                    sendStatisticsMessage(event, emojiStatsConductor.getEmojiStatsService().getEmojiList());
                } catch (InterruptedException | FileNotFoundException e) {
                    sendMessage(event, String.format("Backup **has failed**! [%s]", e.getMessage()));
                } catch (InvalidParameterException ipe) {
                    sendMessage(event, ipe.getMessage());
                } catch (InternalRuntimeError ire) {
                    sendMessage(event, String.format("Calculation failed! [%s]", ire.getMessage()));
                }
            }).start();
        }
    }

    private void sendStatisticsMessage(MessageReceivedEvent event, Map<String, Integer> emojiMap) {
        StringBuilder message = new StringBuilder();
        message.append("Emoji statistics for current channel:\n");

        for (Map.Entry<String, Integer> entry : emojiMap.entrySet()) {
            String markupEmoji;

            // Prepare server emojis for displaying <:emoji:id>
            if (entry.getKey().contains(":")) {
                String emojiName = entry.getKey().replaceAll(":", "");
                try {
                    String emojiId = event.getGuild().getEmotesByName(emojiName, true).get(0).getId();
                    markupEmoji = "<:" + emojiName + ":" + emojiId + ">";
                } catch (IndexOutOfBoundsException iobe) {
                    markupEmoji = entry.getKey();
                }
            } else {
                markupEmoji = entry.getKey();
            }

            message.append(markupEmoji)
                    .append("\t --> \t")
                    .append(entry.getValue())
                    .append("\n");
        }

        if (message.capacity() >= 2000) {
            String[] explodedMessage = message.toString().split("\n");
            List<String> butchMessage = new ArrayList<>();
            int butchSize = 50;

            for (int i = 0; i < explodedMessage.length; i++) {
                butchMessage.add(explodedMessage[i]);

                if (butchMessage.size() == butchSize |
                        i == explodedMessage.length - 1) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : butchMessage) {
                        sb.append(s);
                        sb.append("\n");
                    }
                    sendMessage(event, sb.toString());
                    butchMessage.clear();
                }
            }
        } else {
            sendMessage(event, message.toString());
        }
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(BOT_PREFIX + "emojistats", BOT_PREFIX + "emoji_stats");
    }

    @Override
    public String getDescription() {
        return "Calculate emoji usage statics without reactions";
    }

    @Override
    public String getName() {
        return "Emoji Statistics Command";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(BOT_PREFIX + "emojistats **OR** " + BOT_PREFIX + "emojistats *<arguments>*\n"
                + BOT_PREFIX + "emojistats - returns full statistics **(once in 24h)** of emoji usage in the current channel.\n"
                + BOT_PREFIX + "emojistats <arguments> - " +
                "returns full statistics of emoji usage in the current channel within specified period.\n"
                + "\t\t -b, --before <mm/dd/yyyy> - specifies date till which statics would be done.\n"
                + "\t\t -a, --after  <mm/dd/yyyy> - specifies date from which statics would be done.\n"
                + BOT_PREFIX + "emojistats -i - ignores unicode emoji.\n"
                + BOT_PREFIX + "emojistats -iu - ignores unicode emoji and unknown emoji.\n"
                + BOT_PREFIX + "emojistats -f - creates new backup despite existing one.");
    }
}
