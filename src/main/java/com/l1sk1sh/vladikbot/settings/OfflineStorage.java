package com.l1sk1sh.vladikbot.settings;

import com.l1sk1sh.vladikbot.models.entities.Reminder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class OfflineStorage {
    private transient OfflineStorageManager manager;

    private long lastAutoTextBackupTime = 0;
    private long lastAutoMediaBackupTime = 0;
    private List<Reminder> reminders = new ArrayList<>();

    OfflineStorage(OfflineStorageManager manager) {
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

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void addReminder(Reminder reminder) {
        if (reminders == null) {
            reminders = new ArrayList<>();
        }
        this.reminders.add(reminder);
        manager.writeSettings();
    }

    public void deleteReminder(Reminder reminder) {
        if (reminders == null) {
            reminders = new ArrayList<>();
        }
        this.reminders.remove(reminder);
        manager.writeSettings();
    }
}
