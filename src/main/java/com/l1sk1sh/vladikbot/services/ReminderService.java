package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.entities.Reminder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReminderService {
    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final Bot bot;
    private Reminder reminder;
    private String errorMessage;

    public ReminderService(Bot bot) {
        this.bot = bot;
    }

    public boolean processReminder(String message, String channelId) {
        List<DateGroup> dates = new PrettyTimeParser().parseSyntax(message);

        if (dates.isEmpty()) {
            errorMessage = "Couldn't find any dates in provided string.";
            return false;
        }

        Date reminderDate = dates.get(0).getDates().get(0);
        String reminderText = message.replace(dates.get(0).getText(), "").trim();
        this.reminder = new Reminder(reminderDate, reminderText, channelId);
        bot.getOfflineStorage().addReminder(reminder);

        return scheduleReminder(reminder);
    }

    public boolean scheduleReminder(Reminder reminder) {
        long delay = (reminder.getDateOfReminder().getTime() - new Date().getTime());
        if (delay < 0) {
            errorMessage = "Reminder's date should be in the future.";
            return false;
        }

        bot.getThreadPool().schedule(this::remind, delay, TimeUnit.MILLISECONDS);
        return true;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private void remind() {
        TextChannel textChannel = bot.getJDA().getTextChannelById(reminder.getTextChannelId());
        if (textChannel == null) {
            log.error("Reminder's text channel ({}) is absent.", reminder.getTextChannelId());
            bot.getOfflineStorage().deleteReminder(reminder);
            return;
        }
        textChannel.sendMessage(reminder.getTextOfReminder()).queue();
        bot.getOfflineStorage().deleteReminder(reminder);
    }
}
