package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.entity.Reminder;
import com.l1sk1sh.vladikbot.services.ReminderService;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Service
public class ReminderCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(ReminderCommand.class);

    private final ReminderService reminderService;

    @Autowired
    public ReminderCommand(ReminderService reminderService) {
        this.name = "remind";
        this.aliases = new String[]{"reminder", "remindme"};
        this.help = "set reminder that will be returned by bot at specified time\r\n"
                + "\t\t `<time>` - time in natural language (tomorrow, 12/03/2019, etc). Use english locale for date\r\n"
                + "\t\t `<reminder text>` - what you want to be reminded about\r\n"
                + "Example: *Tomorrow watch nice Ubisoft conference*\r\n"
                + "\t\t `<delete> <id>` - removes reminder by its id\r\n"
                + "\t\t `<list>` - show all scheduled reminders";
        this.arguments = "<<time> <reminder text>|<delete>|<list>";
        this.reminderService = reminderService;
        this.children = new AdminCommand[]{
                new ReadCommand(),
                new DeleteCommand()
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please add reminder with date.");
            return;
        }

        boolean reminderProcessed = reminderService.processReminder(event.getArgs(), event.getChannel().getIdLong(), event.getAuthor().getIdLong());

        if (!reminderProcessed) {
            event.replyError(reminderService.getErrorMessage());
            return;
        }

        Reminder scheduledReminder = reminderService.getReminder();
        log.info("New reminder with id {} was added by {}.", scheduledReminder.getId(), FormatUtils.formatAuthor(event));

        event.reply(String.format("\"%1$s\" will be reminded at %2$s",
                scheduledReminder.getTextOfReminder(),
                scheduledReminder.getDateOfReminder()));
    }

    private final class ReadCommand extends AdminCommand {
        private ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list", "read"};
            this.help = "lists all scheduled reminders";
        }

        @Override
        protected void execute(CommandEvent event) {
            List<Reminder> list = reminderService.getAllReminders();
            if (list == null) {
                event.replyError("Failed to load available reminders!");
            } else if (list.isEmpty()) {
                event.replySuccess("There are no reminders set.");
            } else {
                String message = event.getClient().getSuccess() + " Scheduled reminders:\r\n";
                StringBuilder builder = new StringBuilder(message);
                list.forEach(reminder -> builder.append("`").append(reminder.getDateOfReminder())
                        .append(" ").append(reminder.getTextOfReminder())
                        .append(" (").append(reminder.getId())
                        .append(")`").append("\r\n"));
                event.reply(builder.toString());
            }
        }
    }

    private final class DeleteCommand extends AdminCommand {
        private DeleteCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing reminder";
            this.arguments = "<id of reminder>";
        }

        @Override
        protected void execute(CommandEvent event) {
            String reminderId = event.getArgs().replaceAll("\\s+", " ");

            boolean deleted = reminderService.deleteReminder(Long.parseLong(reminderId));
            if (!deleted) {
                log.error("Failed to delete reminder: `{}`.", reminderService.getErrorMessage());
                event.replyError(String.format("Unable to delete this reminder `[%1$s]`.", reminderService.getErrorMessage()));
                return;
            }

            log.info("Reminder with id {} was removed by {}.", reminderId, FormatUtils.formatAuthor(event));
            event.replySuccess(String.format("Successfully deleted reminder with id `[%1$s]`.", reminderId));
        }
    }
}
