package com.multiheaded.vladikbot.conductors;

import com.multiheaded.vladikbot.conductors.services.BackupChannelService;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.settings.LockdownInterface;
import com.multiheaded.vladikbot.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;

abstract class AbstractBackupConductor {
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

    File prepareFile(String channelId, String format, String localPath, String dockerPath,
                     String containerName, String token, String[] args, LockdownInterface lock)
            throws InterruptedException, InvalidParameterException, IOException {

        File exportedFile = FileUtils.getFileByIdAndExtension(localPath + dockerPath, channelId,
                Constants.FORMAT_EXTENSION.get(format));

        // If file is absent or was made more than 24 hours ago - create new backup
        if (exportedFile == null
                || (System.currentTimeMillis() - exportedFile.lastModified()) > Constants.DAY_IN_MILLISECONDS
                || forceBackup) {

            BackupChannelService backupChannelService = new BackupChannelService(channelId, format, args, localPath,
                    dockerPath, containerName, token, lock);
            exportedFile = backupChannelService.getExportedFile();
            if (exportedFile == null) throw new FileNotFoundException("Failed to find or create backup of a channel");
        }

        return exportedFile;
    }
}
