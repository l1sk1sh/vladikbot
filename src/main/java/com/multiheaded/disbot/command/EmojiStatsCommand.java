package com.multiheaded.disbot.command;

import com.multiheaded.disbot.service.BackupService;
import com.multiheaded.disbot.service.EmojiStatsService;
import com.multiheaded.disbot.util.FileUtils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.File;
import java.util.*;

import static com.multiheaded.disbot.DisBot.settings;
import static com.multiheaded.disbot.settings.Constants.BOT_PREFIX;

public class EmojiStatsCommand extends AbstractCommand {

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        if (!e.isFromType(ChannelType.PRIVATE)) {
            sendMessage(e, "Initializing emoji statistics calculation. Be patient...");

            new Thread(() -> {
                BackupService bs = new BackupService(e.getChannel().getId(), "PlainText");

                if (bs.isCompleted()) {
                    String exportedFile = FileUtils.getFileNameByIdAndExtension(
                            settings.localPathToExport + settings.dockerPathToExport,
                            e.getChannel().getId(),
                            ".txt");
                    EmojiStatsService ess = new EmojiStatsService(new File(Objects.requireNonNull(exportedFile)));

                    if (ess.getEmojiList() != null) {
                        StringBuilder message = new StringBuilder();
                        message.append("Emoji statistics for current channel:\n");

                        for (Map.Entry<String, Integer> entry : ess.getEmojiList().entrySet()) {
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

                        sendMessage(e, message.toString());
                    } else {
                        sendMessage(e, "Oooops! **Stats calculation failed!** Read logs.");
                    }
                } else {
                    sendMessage(e, "Oooops! **Backup system failed!** Read logs.");
                }
            }).start();
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
        return Collections.singletonList(
                BOT_PREFIX + "emoji - returns full statistics of emoji usage in the current channel");
    }
}
