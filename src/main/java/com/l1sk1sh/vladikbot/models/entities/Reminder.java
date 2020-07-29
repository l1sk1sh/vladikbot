package com.l1sk1sh.vladikbot.models.entities;

import java.util.Date;

@SuppressWarnings("unused")
public class Reminder {
    private Date dateOfReminder;
    private String textOfReminder;
    private String textChannelId;

    public Reminder(Date dateOfReminder, String textOfReminder, String textChannelId) {
        this.dateOfReminder = dateOfReminder;
        this.textOfReminder = textOfReminder;
        this.textChannelId = textChannelId;
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

    @Override
    public String toString() {
        return "Reminder{" +
                "dateOfReminder=" + dateOfReminder +
                ", textOfReminder='" + textOfReminder + '\'' +
                ", textChannelId='" + textChannelId + '\'' +
                '}';
    }
}
