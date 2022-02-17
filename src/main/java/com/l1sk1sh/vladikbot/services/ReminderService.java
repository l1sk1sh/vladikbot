package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.data.entity.Reminder;
import com.l1sk1sh.vladikbot.data.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author l1sk1sh
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ReminderService {

    @Qualifier("frontThreadPool")
    private final ScheduledExecutorService frontThreadPool;
    private final ReminderRepository reminderRepository;
    private Reminder reminder;
    private String errorMessage;
    private final Map<Long, ScheduledFuture<?>> scheduledReminders = new HashMap<>();

    public boolean processReminder(String timeMessage, String reminderText, long channelId, long authorId) {
        List<DateGroup> dates = new PrettyTimeParser().parseSyntax(timeMessage);

        if (dates.isEmpty()) {
            errorMessage = "Couldn't find any dates in provided string.";
            return false;
        }

        Date reminderDate = dates.get(0).getDates().get(0);
        Reminder reminder = new Reminder(reminderDate, reminderText.trim(), channelId, authorId);
        reminderRepository.save(reminder);

        return scheduleReminder(reminder);
    }

    public boolean scheduleReminder(Reminder reminder) {
        log.info("Scheduling reminder '{}'", reminder);
        this.reminder = reminder;
        long delay = (reminder.getDateOfReminder().getTime() - new Date().getTime());
        if (delay < 0) {
            errorMessage = "Reminder's date should be in the future.";
            return false;
        }

        Runnable remindEvent = () -> {
            TextChannel textChannel = VladikBot.jda().getTextChannelById(reminder.getTextChannelId());
            if (textChannel == null) {
                log.error("Reminder's text channel ({}) is absent.", reminder.getTextChannelId());
                reminderRepository.delete(reminder);
                return;
            }

            User author = VladikBot.jda().getUserById(reminder.getAuthorId());

            if (author == null) {
                log.warn("Author of reminder is no long present.");
                reminderRepository.delete(reminder);
                return;
            }

            String reminderMessage = String.format("%1$s: \"%2$s\"", author.getAsMention(), reminder.getTextOfReminder());
            textChannel.sendMessage(reminderMessage).queue();
            reminderRepository.delete(reminder);
        };

        ScheduledFuture<?> scheduledReminder = frontThreadPool.schedule(remindEvent, delay, TimeUnit.MILLISECONDS);
        scheduledReminders.put(reminder.getId(), scheduledReminder);

        return true;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    public boolean deleteReminder(long reminderId) {
        Reminder reminder = getAllReminders().stream().filter(r -> r.getId() == (reminderId)).findFirst().orElse(null);

        if (reminder == null) {
            errorMessage = "Failed to find reminder with provided id.";
            return false;
        }

        reminderRepository.delete(reminder);
        ScheduledFuture<?> scheduled = scheduledReminders.get(reminderId);

        if (scheduled == null) {
            log.warn("Provided reminder is not scheduled.");
            return true;
        }

        scheduled.cancel(false);

        return true;
    }
}
