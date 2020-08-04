package com.l1sk1sh.vladikbot.settings;

import com.l1sk1sh.vladikbot.models.entities.Reminder;
import com.l1sk1sh.vladikbot.services.rss.RssResource;
import org.apache.commons.collections4.queue.CircularFifoQueue;
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
    private Map<RssResource, CircularFifoQueue<String>> lastSentArticles = new HashMap<>();
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

    public CircularFifoQueue<String> getLastArticleIds(@NotNull RssResource resource) {
        if (lastSentArticles == null) {
            lastSentArticles = new HashMap<>();
        }
        CircularFifoQueue<String> queue = new CircularFifoQueue<>(Const.ARTICLE_FETCH_LIMIT);
        CircularFifoQueue<String> storedQueue = lastSentArticles.getOrDefault(resource, null);
        if (storedQueue != null) {
            queue.addAll(storedQueue);
        }
        return queue;
    }

    public void setLastArticleIds(@NotNull RssResource resource, CircularFifoQueue<String> listOfSentArticles) {
        if (lastSentArticles == null) {
            lastSentArticles = new HashMap<>();
        }
        lastSentArticles.put(resource, listOfSentArticles);
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
