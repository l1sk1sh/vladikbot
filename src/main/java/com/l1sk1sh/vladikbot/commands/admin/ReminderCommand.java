package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.entity.Reminder;
import com.l1sk1sh.vladikbot.services.ReminderService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class ReminderCommand extends AdminCommand {

    private final ReminderService reminderService;

    @Autowired
    public ReminderCommand(BotSettingsManager settings, ReminderService reminderService) {
        this.name = "remind";
        this.help = "Sets reminder that will be returned to you by bot at specified time";
        this.reminderService = reminderService;
        this.children = new AdminCommand[]{
                new CreateCommand(),
                new ReadCommand(settings),
                new DeleteCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString()).setEphemeral(true).queue();
    }

    private final class CreateCommand extends AdminCommand {

        private static final String TIME_OPTION_KEY = "time";
        private static final String TEXT_OPTION_KEY = "text";
        private static final String TAG_AUTHOR_OPTION_KEY = "author";
        private static final String REPEAT_OPTION_KEY = "repeat";

        private CreateCommand() {
            this.name = "create";
            this.help = "Create new reminder";
            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, TIME_OPTION_KEY, "Time in natural language (tomorrow, 12/03/2019, etc)").setRequired(true));
            options.add(new OptionData(OptionType.STRING, TEXT_OPTION_KEY, "Reminder text").setRequired(true));
            options.add(new OptionData(OptionType.BOOLEAN, TAG_AUTHOR_OPTION_KEY, "Tag author").setRequired(false));
            options.add(new OptionData(OptionType.STRING, REPEAT_OPTION_KEY, "Should repeat reminder").setRequired(false)
                    .addChoice("Repeat daily", "daily")
                    .addChoice("Repeat weekly", "weekly")
                    .addChoice("Repeat monthly", "monthly")
                    .addChoice("Repeat yearly", "yearly"));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping timeOption = event.getOption(TIME_OPTION_KEY);
            if (timeOption == null) {
                event.replyFormat("%1$s Specify time for reminder!",
                        event.getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            OptionMapping textOption = event.getOption(TEXT_OPTION_KEY);
            if (textOption == null) {
                event.replyFormat("%1$s Reminder text should not be empty!",
                        event.getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            OptionMapping repeatOption = event.getOption(REPEAT_OPTION_KEY);
            Reminder.RepeatPeriod period = null;
            if (repeatOption != null) {
                try {
                    period = Reminder.RepeatPeriod.valueOf(repeatOption.getAsString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    event.replyFormat("%1$s Specify either `yearly`, `monthly`, `weekly` or `daily` period.", event.getClient().getWarning()).setEphemeral(true).queue();

                    return;
                }
            }

            OptionMapping authorOption = event.getOption(TAG_AUTHOR_OPTION_KEY);

            boolean tagAuthor = (authorOption == null) || authorOption.getAsBoolean();
            boolean reminderProcessed = reminderService.processReminder(timeOption.getAsString(), textOption.getAsString(),
                    event.getChannel().getIdLong(), event.getUser().getIdLong(), period, tagAuthor);

            if (!reminderProcessed) {
                event.replyFormat("%1$s %2$s", event.getClient().getError(), reminderService.getErrorMessage()).setEphemeral(true).queue();
                return;
            }

            Reminder scheduledReminder = reminderService.getLatestReminder();
            log.info("New reminder with id {} was added by {}.", scheduledReminder.getId(), FormatUtils.formatAuthor(event));

            event.replyFormat("%1$s \"%2$s\" will be reminded at %3$s",
                    event.getClient().getSuccess(),
                    scheduledReminder.getTextOfReminder(),
                    scheduledReminder.getDateOfReminder()).setEphemeral(true).queue();
        }
    }

    private final class ReadCommand extends AdminCommand {

        private final BotSettingsManager settings;

        private ReadCommand(BotSettingsManager settings) {
            this.settings = settings;
            this.name = "list";
            this.help = "Lists all scheduled reminders";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            List<Reminder> list;
            if (event.getUser().getIdLong() == settings.get().getOwnerId()) {
                list = reminderService.getAllReminders();
            } else {
                list = reminderService.getRemindersByAuthor(event.getUser().getIdLong());
            }

            if (list == null) {
                event.replyFormat("%1$s Failed to load available reminders!", event.getClient().getError()).setEphemeral(true).queue();
            } else if (list.isEmpty()) {
                event.replyFormat("%1$s Failed to load available reminders!", event.getClient().getSuccess()).setEphemeral(true).queue();
            } else {
                String message = event.getClient().getSuccess() + " Scheduled reminders:\r\n";
                StringBuilder builder = new StringBuilder(message);
                list.forEach(reminder -> builder.append("`").append(reminder.getDateOfReminder())
                        .append(" ").append(reminder.getTextOfReminder())
                        .append(" (").append(reminder.getId())
                        .append(")`").append("\r\n"));
                event.reply(builder.toString()).setEphemeral(true).queue();
            }
        }
    }

    private final class DeleteCommand extends AdminCommand {

        private static final String ID_OPTION_KEY = "id";

        private DeleteCommand() {
            this.name = "delete";
            this.help = "Deletes an existing reminder";
            this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, ID_OPTION_KEY, "Id of the reminder").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping idOption = event.getOption(ID_OPTION_KEY);
            if (idOption == null) {
                event.replyFormat("%1$s ID is required for deletion. Use 'list' command to find ID of reminder.", event.getClient().getWarning()).setEphemeral(true).queue();
                return;
            }
            long reminderId = idOption.getAsLong();

            boolean deleted = reminderService.deleteReminder(reminderId);
            if (!deleted) {
                log.error("Failed to delete reminder: `{}`.", reminderService.getErrorMessage());
                event.replyFormat("%1$s Unable to delete this reminder `[%2$s]`.", event.getClient().getError(), reminderService.getErrorMessage()).setEphemeral(true).queue();
                return;
            }

            log.info("Reminder with id {} was removed by {}.", reminderId, FormatUtils.formatAuthor(event));
            event.replyFormat("%1$s Successfully deleted reminder with id `[%2$s]`.", event.getClient().getSuccess(), reminderId).setEphemeral(true).queue();
        }
    }
}
