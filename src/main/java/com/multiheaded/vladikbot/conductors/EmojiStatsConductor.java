package com.multiheaded.vladikbot.conductors;

import com.multiheaded.vladikbot.conductors.services.EmojiStatsService;
import com.multiheaded.vladikbot.settings.LockdownInterface;
import net.dv8tion.jda.core.entities.Emote;

import java.io.IOException;
import java.util.List;

/**
 * @author Oliver Johnson
 */
public class EmojiStatsConductor extends AbstractBackupConductor {
    private EmojiStatsService emojiStatsService;

    public EmojiStatsConductor(String channelId, String format, String localPath, String dockerPath,
                               String containerName, String token, String[] args, List<Emote> serverEmojiList,
                               LockdownInterface lock)
            throws InterruptedException, IOException {
        this.args = args;

        processArguments();
        emojiStatsService = new EmojiStatsService(
                prepareFile(channelId, format, localPath, dockerPath, containerName, token, args, lock),
                serverEmojiList,
                args);
    }

    public EmojiStatsService getEmojiStatsService() {
        return emojiStatsService;
    }
}
