package com.multiheaded.disbot.models.conductors;

import com.multiheaded.disbot.models.conductors.services.BackupService;
import com.multiheaded.disbot.settings.Settings;

import java.security.InvalidParameterException;

/**
 * @author Oliver Johnson
 */
public class BackupConductor {
    private static final String format = "HtmlDark";

    private BackupService backupService;

    public BackupConductor(String channelId, String[] args, Settings settings)
            throws InterruptedException, InvalidParameterException {
        backupService = new BackupService(channelId, format, args, settings.getLocalPathToExport(),
                settings.getDockerPathToExport(), settings.getDockerContainerName(), settings.getToken());
    }

    public BackupService getBackupService() {
        return backupService;
    }
}
