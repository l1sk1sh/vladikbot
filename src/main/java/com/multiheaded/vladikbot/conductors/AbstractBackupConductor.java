package com.multiheaded.vladikbot.conductors;

import com.multiheaded.vladikbot.conductors.services.BackupChannelService;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;
import com.multiheaded.vladikbot.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;

abstract class AbstractBackupConductor {
    final Settings settings = SettingsManager.getInstance().getSettings();
    static final String format = "PlainText";
    private boolean forceBackup;
    String[] args;

    void processArguments() {
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

    File prepareFile(String channelId, String extension, String[] args)
            throws InterruptedException, InvalidParameterException, IOException {
        File exportedFile = FileUtils.getFileByIdAndExtension(
                settings.getLocalPathToExport() + settings.getDockerPathToExport(), channelId, extension);

        // If file is absent or was made more than 24 hours ago - create new backup
        if (exportedFile == null
                || (System.currentTimeMillis() - exportedFile.lastModified()) > Constants.DAY_IN_MILLISECONDS
                || forceBackup) {

            BackupChannelService backupChannelService =
                    new BackupChannelService(channelId, format, args, settings.getLocalPathToExport(),
                            settings.getDockerPathToExport(), settings.getDockerContainerName(), settings.getToken());
            exportedFile = backupChannelService.getExportedFile();
            if (exportedFile == null) throw new FileNotFoundException("Failed to find or create backup of a channel");
        }

        return exportedFile;
    }
}
