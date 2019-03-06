package com.multiheaded.disbot.command;

import com.multiheaded.disbot.service.BackupService;
import com.multiheaded.disbot.service.EmojiStatsService;
import com.multiheaded.disbot.settings.Constants;
import com.multiheaded.disbot.util.FileUtils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.File;
import java.util.*;

import static com.multiheaded.disbot.DisBot.settings;
import static com.multiheaded.disbot.settings.Constants.BOT_PREFIX;
import static com.multiheaded.disbot.settings.Constants.FORMAT_EXTENSION;

public class EmojiStatsCommand extends AbstractCommand {

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        String format = "PlainText";
        String extension = FORMAT_EXTENSION.get(format);

        if (!e.isFromType(ChannelType.PRIVATE)) {
            sendMessage(e, "Initializing emoji statistics calculation. Be patient...");

            new Thread(() -> {
                String exportedFilePath = prepareFilePath(e.getChannel().getId(), extension, format, args);

                if (exportedFilePath != null) {
                    EmojiStatsService ess = new EmojiStatsService(
                            new File(Objects.requireNonNull(exportedFilePath)), e.getGuild().getEmotes(), args);

                    if (ess.getEmojiList() != null) {
                        sendStatisticsMessage(e, ess.getEmojiList());
                    } else {
                        sendMessage(e, "Oooops! **Stats calculation failed!** Read logs.");
                    }
                } else {
                    sendMessage(e, "Oooops! **Backup failed!** Read logs.");
                }
            }).start();
        }
    }

    private String prepareFilePath(String channelId, String extension, String format, String[] args) {
        String exportedFilePath = FileUtils.getFileNameByIdAndExtension(
                settings.localPathToExport + settings.dockerPathToExport,
                channelId, extension);

        // If file is absent or was made more than 24 hours ago - create new backup
        if (exportedFilePath == null
                || (System.currentTimeMillis() - new File(exportedFilePath).lastModified()) > Constants.DAY_IN_MILISECONDS) {
            BackupService bs = new BackupService(channelId, format, args);

            if (bs.isCompleted()) {
                exportedFilePath = FileUtils.getFileNameByIdAndExtension(
                        settings.localPathToExport + settings.dockerPathToExport,
                        channelId, extension);
            }
        }

        return exportedFilePath;
    }

    private void sendStatisticsMessage(MessageReceivedEvent e, Map<String, Integer> emojiMap) {
        StringBuilder message = new StringBuilder();
        message.append("Emoji statistics for current channel:\n");

        for (Map.Entry<String, Integer> entry : emojiMap.entrySet()) {
            String markupEmoji;

            // Prepare server emojis for displaying <:emoji:id>
            if (entry.getKey().contains(":")) {
                String emojiName = entry.getKey().replaceAll(":", "");
                try {
                    String emojiId = e.getGuild().getEmotesByName(emojiName, true).get(0).getId();
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
                    sendMessage(e, sb.toString());
                    butchMessage.clear();
                }
            }
        } else {
            sendMessage(e, message.toString());
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
                + BOT_PREFIX + "emojistats -iu - ignores unicode emoji and unknown emoji.");
    }
}
