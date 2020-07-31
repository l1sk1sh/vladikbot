package com.l1sk1sh.vladikbot.settings;

import com.l1sk1sh.vladikbot.models.entities.Reminder;
import com.l1sk1sh.vladikbot.services.rss.RssResource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class OfflineStorage {
    private transient OfflineStorageManager manager;

    private long lastAutoTextBackupTime = 0;
    private long lastAutoMediaBackupTime = 0;
    private Map<RssResource, Long> lastSentArticles = new HashMap<>();
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

    public long getLastArticleTime(@NotNull RssResource resource) {
        if (lastSentArticles == null) {
            lastSentArticles = new HashMap<>();
        }
        return (lastSentArticles.containsKey(resource)) ? lastSentArticles.get(resource) : 0;
    }

    public void setLastArticleTime(@NotNull RssResource resource, long lastArticleTime) {
        if (lastSentArticles == null) {
            lastSentArticles = new HashMap<>();
        }
        lastSentArticles.put(resource, lastArticleTime);
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
