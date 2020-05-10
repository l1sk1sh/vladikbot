package com.l1sk1sh.vladikbot.settings;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class OfflineStorage {
    private transient OfflineStorageManager manager;

    private long lastAutoTextBackupTime = 0;
    private long lastAutoMediaBackupTime = 0;

    public OfflineStorage(OfflineStorageManager manager) {
        this.manager = manager;
    }

    final void setManager(OfflineStorageManager manager) {
        this.manager = manager;
    }

    public long getLastAutoTextBackupTime() {
        return lastAutoTextBackupTime;
    }

    public void setLastAutoTextBackupTime(long lastAutoTextBackupTime) {
        this.lastAutoTextBackupTime = lastAutoTextBackupTime;
        manager.writeSettings();
    }

    public long getLastAutoMediaBackupTime() {
        return lastAutoMediaBackupTime;
    }

    public void setLastAutoMediaBackupTime(long lastAutoMediaBackupTime) {
        this.lastAutoMediaBackupTime = lastAutoMediaBackupTime;
        manager.writeSettings();
    }
}
