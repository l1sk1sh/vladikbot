package com.l1sk1sh.vladikbot.models.entities;

import java.util.Date;

@SuppressWarnings("unused")
public class Reminder {
    private long id;
    private Date dateOfReminder;
    private String textOfReminder;
    private String textChannelId;
    private String authorId;

    public Reminder(Date dateOfReminder, String textOfReminder, String textChannelId, String authorId) {
        this.id = System.currentTimeMillis();
        this.dateOfReminder = dateOfReminder;
        this.textOfReminder = textOfReminder;
        this.textChannelId = textChannelId;
        this.authorId = authorId;
    }

    public long getId() {
        return id;
    }

    public Date getDateOfReminder() {
        return dateOfReminder;
    }

    public void setDateOfReminder(Date dateOfReminder) {
        this.dateOfReminder = dateOfReminder;
    }

    public String getTextOfReminder() {
        return textOfReminder;
    }

    public void setTextOfReminder(String textOfReminder) {
        this.textOfReminder = textOfReminder;
    }

    public String getTextChannelId() {
        return textChannelId;
    }

    public void setTextChannelId(String textChannelId) {
        this.textChannelId = textChannelId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "dateOfReminder=" + dateOfReminder +
                ", textOfReminder='" + textOfReminder + '\'' +
                ", textChannelId='" + textChannelId + '\'' +
                '}';
    }
}
