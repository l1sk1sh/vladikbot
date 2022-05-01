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

import java.util.*;
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
    private Reminder latestReminder;
    private String errorMessage;
    private final Map<Long, ScheduledFuture<?>> scheduledReminders = new HashMap<>();

    public boolean processReminder(String timeMessage, String reminderText, long channelId, long authorId, Reminder.RepeatPeriod repeatPeriod, boolean tagAuthor) {
        List<DateGroup> dates = new PrettyTimeParser().parseSyntax(timeMessage);

        if (dates.isEmpty()) {
            errorMessage = "Couldn't find any dates in provided string.";
            return false;
        }

        Date reminderDate = dates.get(0).getDates().get(0);
        Reminder reminder = Reminder.builder()
                .dateOfReminder(reminderDate)
                .textOfReminder(reminderText.trim())
                .textChannelId(channelId)
                .tagAuthor(tagAuthor)
                .authorId(authorId)
                .repeat(repeatPeriod != null)
                .repeatPeriod(repeatPeriod)
                .build();

        reminderRepository.save(reminder);

        return scheduleReminder(reminder);
    }

    public boolean scheduleReminder(Reminder reminder) {
        log.info("Scheduling reminder '{}'", reminder);

        if (scheduledReminders.containsKey(reminder.getId())) {
            errorMessage = "Already scheduled.";
            return false;
        }

        this.latestReminder = reminder;
        long delay = (reminder.getDateOfReminder().getTime() - new Date().getTime());
        if (delay < 0) {
            errorMessage = "Reminder's date should be in the future.";
            return false;
        }

        Runnable remindEvent = new ReminderTask(reminder, this);

        ScheduledFuture<?> scheduledReminder = frontThreadPool.schedule(remindEvent, delay, TimeUnit.MILLISECONDS);
        scheduledReminders.put(reminder.getId(), scheduledReminder);

        return true;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Reminder getLatestReminder() {
        return latestReminder;
    }

    public List<Reminder> getAllReminders() {
        return reminderRepository.findAll();
    }

    public List<Reminder> getRemindersByAuthor(long authorId) {
        return reminderRepository.findAllByAuthorId(authorId);
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

    @RequiredArgsConstructor
    private class ReminderTask implements Runnable {

        private final Reminder reminder;
        private final ReminderService reminderService;

        @Override
        public void run() {
            TextChannel textChannel = VladikBot.jda().getTextChannelById(reminder.getTextChannelId());
            if (textChannel == null) {
                log.error("Reminder's text channel ({}) is absent.", reminder.getTextChannelId());
                reminderRepository.delete(reminder);
                return;
            }

            User author = VladikBot.jda().getUserById(reminder.getAuthorId());
            if (reminder.isTagAuthor() && author == null) {
                log.warn("Author of reminder is no longer present.");
                reminderRepository.delete(reminder);
                return;
            }

            String reminderMessage;
            if (reminder.isTagAuthor()) {
                reminderMessage = String.format("%1$s: \"%2$s\"", Objects.requireNonNull(author).getAsMention(), reminder.getTextOfReminder());
            } else {
                reminderMessage = String.format("%1$s", reminder.getTextOfReminder());
            }
            textChannel.sendMessage(reminderMessage).queue();
            reminderRepository.delete(reminder);

            if (!reminder.isRepeat()) {
                return;
            }

            long nextReminderTime = System.currentTimeMillis() + reminder.getRepeatPeriod().getDelay();

            Reminder repeatReminder = new Reminder(reminder);

            repeatReminder.getDateOfReminder().setTime(nextReminderTime);
            reminderService.scheduleReminder(repeatReminder);
        }
    }
}
