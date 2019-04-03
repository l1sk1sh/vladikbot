package com.multiheaded.vladikbot.conductors;

import com.multiheaded.vladikbot.conductors.services.BackupMediaService;
import com.multiheaded.vladikbot.models.LockdownInterface;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class BackupMediaConductor extends AbstractBackupConductor {
    private BackupMediaService backupMediaService;

    public BackupMediaConductor(String channelId, String fileName, String format, String localPath, String dockerPath,
                                String containerName, String token, String[] args, LockdownInterface lock)
            throws InterruptedException, IOException {

        this.args = args;
        processArguments();

        backupMediaService = new BackupMediaService(
                channelId,
                prepareFile(channelId, format, localPath, dockerPath, containerName, token, args, lock),
                localPath,
                fileName,
                args,
                lock);
    }

    public BackupMediaService getBackupMediaService() {
        return backupMediaService;
    }
}
