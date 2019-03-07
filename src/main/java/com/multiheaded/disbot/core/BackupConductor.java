package com.multiheaded.disbot.core;

import com.multiheaded.disbot.core.service.BackupService;

import java.security.InvalidParameterException;

public class BackupConductor {
    private static final String format = "HtmlDark";

    private BackupService backupService;

    public BackupConductor(String channelId, String[] args)
            throws InterruptedException, InvalidParameterException {
        backupService = new BackupService(channelId, format, args);
    }

    public BackupService getBackupService() {
        return backupService;
    }
}
