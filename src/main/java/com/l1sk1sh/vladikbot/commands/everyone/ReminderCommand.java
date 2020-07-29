package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.entities.Reminder;
import com.l1sk1sh.vladikbot.services.ReminderService;

/**
 * @author Oliver Johnson
 */
public class ReminderCommand extends Command {
    private final ReminderService reminderService;

    public ReminderCommand(Bot bot) {
        this.name = "remind";
        this.aliases = new String[]{"reminder", "remindme"};
        this.help = "set reminder that will be returned by bot at specified time\r\n"
                + "\t\t `<time>` - time in natural language (tomorrow, 12/03/2019, etc). Use english locale for date\r\n"
                + "\t\t `<reminder text>` - what you want to be reminded about\r\n" +
                "Example: *Tomorrow watch nice Ubisoft conference*";
        this.arguments = "<time> <reminder text>";
        this.reminderService = bot.getReminderService();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please add reminder with date.");
            return;
        }

        boolean reminderProcessed = reminderService.processReminder(event.getArgs(), event.getChannel().getId());

        if (!reminderProcessed) {
            event.replyError(reminderService.getErrorMessage());
            return;
        }

        Reminder scheduledReminder = reminderService.getReminder();

        event.reply(String.format("\"%1$s\" will be reminded at %2$s",
                scheduledReminder.getTextOfReminder(),
                scheduledReminder.getDateOfReminder()));
    }
}
