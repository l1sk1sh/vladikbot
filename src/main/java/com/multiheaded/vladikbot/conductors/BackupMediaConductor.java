package com.multiheaded.vladikbot.conductors;

import com.multiheaded.vladikbot.conductors.services.BackupMediaService;

import java.io.IOException;

import static com.multiheaded.vladikbot.settings.Constants.FORMAT_EXTENSION;

/**
 * @author Oliver Johnson
 */
public class BackupMediaConductor extends AbstractBackupConductor {
    private BackupMediaService backupMediaService;

    public BackupMediaConductor(String channelId, String fileName, String[] args)
            throws InterruptedException, IOException {

        this.args = args;
        processArguments();

        backupMediaService = new BackupMediaService(channelId,
                prepareFile(channelId, FORMAT_EXTENSION.get(format), args),
                settings.getLocalPathToExport(),
                fileName,
                args);
    }

    public BackupMediaService getBackupMediaService() {
        return backupMediaService;
    }
}
