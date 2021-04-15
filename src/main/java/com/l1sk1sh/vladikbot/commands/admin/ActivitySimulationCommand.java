package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.entity.Activity;
import com.l1sk1sh.vladikbot.services.presence.ActivitySimulationManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author Oliver Johnson
 */
@Service
public class ActivitySimulationCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(ActivitySimulationCommand.class);

    private final BotSettingsManager settings;
    private final ActivitySimulationManager activitySimulationManager;

    @Autowired
    public ActivitySimulationCommand(BotSettingsManager settings, ActivitySimulationManager activitySimulationManager) {
        this.settings = settings;
        this.activitySimulationManager = activitySimulationManager;
        this.name = "simactivity";
        this.aliases = new String[]{"simactivity"};
        this.help = "Activity simulation management";
        this.arguments = "<add|list|switch|delete>";
        this.guildOnly = false;
        this.children = new AdminCommand[]{
                new CreateCommand(),
                new ReadCommand(),
                new SwitchCommand(),
                new DeleteCommand()
        };
    }

    @Override
    protected final void execute(CommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
    }

    class CreateCommand extends AdminCommand {
        CreateCommand() {
            this.name = "create";
            this.aliases = new String[]{"make", "add"};
            this.help = "creates new activity rule";
            this.arguments = "<action> <activity>"; /* Used twisted to simplify UX (for ex. 'playing WoW', instead of 'WoW playing') */
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.replyError("Please include pair *action* *activity*!");
                return;
            }

            Const.StatusAction action;
            if (event.getArgs().toLowerCase().startsWith(Const.StatusAction.playing.name())) {
                action = Const.StatusAction.playing;
            } else if (event.getArgs().toLowerCase().startsWith(Const.StatusAction.listening.name())) {
                action = Const.StatusAction.listening;
            } else if (event.getArgs().toLowerCase().startsWith(Const.StatusAction.watching.name())) {
                action = Const.StatusAction.watching;
            } else {
                event.replyWarning(String.format("Action word must be one of `[%1$s, %2$s, %3$s]`!",
                        Const.StatusAction.playing,
                        Const.StatusAction.listening,
                        Const.StatusAction.watching));
                return;
            }

            Activity newPair = new Activity(
                    event.getArgs().substring(action.name().length()).trim(), action);

            activitySimulationManager.writeRule(newPair);
            log.info("Added new rule to ActivitySimulation: {}.", newPair);
            event.replySuccess("New rule was added.");
        }
    }

    class ReadCommand extends AdminCommand {
        ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list", "read"};
            this.help = "lists all available rules";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            List<Activity> list = activitySimulationManager.getAllRules();
            if (list == null) {
                event.replyError("Failed to load available rules!");
            } else if (list.isEmpty()) {
                event.replyWarning("There are no records available!");
            } else {
                String message = event.getClient().getSuccess() + " Acting rules:\r\n";
                StringBuilder builder = new StringBuilder(message);
                list.forEach(rule -> builder.append("`").append(rule.getStatusAction()).append(" ").append(rule.getActivityName()).append("`").append("\r\n"));
                event.reply(builder.toString());
            }
        }
    }

    class SwitchCommand extends AdminCommand {
        SwitchCommand() {
            this.name = "switch";
            this.aliases = new String[]{"change"};
            this.help = "enables or disables simulation of bot's activity";
            this.arguments = "<on|off>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length > 0) {
                for (String arg : args) {
                    switch (arg) {
                        case "on":
                        case "enable":
                            settings.get().setSimulateActivity(true);
                            event.replySuccess("Simulation of bot's activity is now enabled!");
                            activitySimulationManager.start();
                            break;
                        case "off":
                        case "disable":
                            settings.get().setSimulateActivity(false);
                            event.replySuccess("Simulation of bot's activity is now disabled!");
                            activitySimulationManager.stop();
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    class DeleteCommand extends AdminCommand {
        DeleteCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing activity rule";
            this.arguments = "<activity>";
            this.guildOnly = false;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String activityName = event.getArgs().replaceAll("\\s+", " ");

            try {
                activitySimulationManager.deleteRule(activityName);
                log.info("ActivitySimulation rule with activity {} was removed by {}.", activityName, FormatUtils.formatAuthor(event));
                event.replySuccess(String.format("Successfully deleted rule with activity `[%1$s]`.", activityName));
            } catch (IOException ioe) {
                log.error("IO error during removal of ActivitySimulation rule", ioe);
                event.replyError(String.format("Unable to delete this rule, that has activity `[%1$s]`.", ioe.getLocalizedMessage()));
            }
        }
    }
}
