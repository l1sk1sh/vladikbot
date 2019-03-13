package com.multiheaded.disbot.models.conductors;

import com.multiheaded.disbot.models.conductors.services.BackupService;
import com.multiheaded.disbot.models.conductors.services.EmojiStatsService;
import com.multiheaded.disbot.settings.Constants;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import com.multiheaded.disbot.utils.FileUtils;
import net.dv8tion.jda.core.entities.Emote;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidParameterException;
import java.util.List;

import static com.multiheaded.disbot.settings.Constants.FORMAT_EXTENSION;

/**
 * @author Oliver Johnson
 */
public class EmojiStatsConductor {
    private static final String format = "PlainText";
    private Settings settings = SettingsManager.getInstance().getSettings();
    private EmojiStatsService emojiStatsService;
    private boolean forceBackup;
    private String[] args;

    public EmojiStatsConductor(String channelId, String[] args, List<Emote> serverEmojiList)
            throws InterruptedException, FileNotFoundException {
        this.args = args;

        processArguments();
        emojiStatsService = new EmojiStatsService(
                prepareFile(channelId, FORMAT_EXTENSION.get(format), format, args),
                serverEmojiList,
                args);
    }

    private File prepareFile(String channelId, String extension, String format, String[] args)
            throws InterruptedException, InvalidParameterException, FileNotFoundException {
        File exportedFile = FileUtils.getFileByIdAndExtension(
                settings.getLocalPathToExport() + settings.getDockerPathToExport(), channelId, extension);

        // If file is absent or was made more than 24 hours ago - create new backup
        if (exportedFile == null
                || (System.currentTimeMillis() - exportedFile.lastModified()) > Constants.DAY_IN_MILLISECONDS
                || forceBackup) {

            BackupService backupService = new BackupService(channelId, format, args, settings.getLocalPathToExport(),
                    settings.getDockerPathToExport(), settings.getDockerContainerName(), settings.getToken());
            exportedFile = backupService.getExportedFile();
            if (exportedFile == null) throw new FileNotFoundException("Failed to find\\create backup of a channel");
        }

        return exportedFile;
    }

    private void processArguments() {
        if (args.length > 0) {
            for (String arg : args) {
                switch (arg) {
                    case "-f":
                        forceBackup = true;
                        break;
                }
            }
        }
    }

    public EmojiStatsService getEmojiStatsService() {
        return emojiStatsService;
    }
}
