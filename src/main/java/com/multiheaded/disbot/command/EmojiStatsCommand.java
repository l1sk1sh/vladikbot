package com.multiheaded.disbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.core.EmojiStatsConductor;
import com.sun.org.apache.xalan.internal.xsltc.runtime.InternalRuntimeError;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.*;

public class EmojiStatsCommand extends Command {

    public EmojiStatsCommand() {
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
    }

    @Override
    public void execute(CommandEvent event) {

        if (!event.isFromType(ChannelType.PRIVATE)) {
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
    }

    private void sendStatisticsMessage(CommandEvent event, Map<String, Integer> emojiMap) {
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
                    event.reply(sb.toString());
                    butchMessage.clear();
                }
            }
        } else {
            event.reply(message.toString());
        }
    }
}
