package com.multiheaded.vladikbot.conductors;

import com.multiheaded.vladikbot.conductors.services.EmojiStatsService;
import net.dv8tion.jda.core.entities.Emote;

import java.io.IOException;
import java.util.List;

import static com.multiheaded.vladikbot.settings.Constants.FORMAT_EXTENSION;

/**
 * @author Oliver Johnson
 */
public class EmojiStatsConductor extends AbstractBackupConductor {
    private EmojiStatsService emojiStatsService;

    public EmojiStatsConductor(String channelId, String[] args, List<Emote> serverEmojiList)
            throws InterruptedException, IOException {
        this.args = args;

        processArguments();
        emojiStatsService = new EmojiStatsService(
                prepareFile(channelId, FORMAT_EXTENSION.get(format), args),
                serverEmojiList,
                args);
    }

    public EmojiStatsService getEmojiStatsService() {
        return emojiStatsService;
    }
}
