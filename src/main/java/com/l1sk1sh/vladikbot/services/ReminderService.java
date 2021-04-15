package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.data.entity.Reminder;
import com.l1sk1sh.vladikbot.data.repository.ReminderRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author Oliver Johnson
 */
@Service
public class ReminderService {
    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private final JDA jda;
    private final ScheduledExecutorService frontThreadPool;
    private final ReminderRepository reminderRepository;
    private Reminder reminder;
    private String errorMessage;
    private final Map<Long, ScheduledFuture<?>> scheduledReminders;

    @Autowired
    public ReminderService(JDA jda, @Qualifier("frontThreadPool") ScheduledExecutorService frontThreadPool, ReminderRepository reminderRepository) {
        this.jda = jda;
        this.frontThreadPool = frontThreadPool;
        this.reminderRepository = reminderRepository;
        this.scheduledReminders = new HashMap<>();
    }

    public boolean processReminder(String message, long channelId, long authorId) {
        List<DateGroup> dates = new PrettyTimeParser().parseSyntax(message);

        if (dates.isEmpty()) {
            errorMessage = "Couldn't find any dates in provided string.";
            return false;
        }

        Date reminderDate = dates.get(0).getDates().get(0);
        String reminderText = message.replace(dates.get(0).getText(), "").trim();
        Reminder reminder = new Reminder(reminderDate, reminderText, channelId, authorId);
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
            TextChannel textChannel = jda.getTextChannelById(reminder.getTextChannelId());
            if (textChannel == null) {
                log.error("Reminder's text channel ({}) is absent.", reminder.getTextChannelId());
                reminderRepository.delete(reminder);
                return;
            }

            User author = jda.getUserById(reminder.getAuthorId());

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
