package com.l1sk1sh.vladikbot.commands.admin;

import com.l1sk1sh.vladikbot.data.entity.Activity;
import com.l1sk1sh.vladikbot.services.presence.ActivitySimulationManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author l1sk1sh
 */
@Service
public class ActivitySimulationCommand extends AdminV2Command {
    private static final Logger log = LoggerFactory.getLogger(ActivitySimulationCommand.class);

    private final BotSettingsManager settings;
    private final ActivitySimulationManager activitySimulationManager;

    @Autowired
    public ActivitySimulationCommand(BotSettingsManager settings, ActivitySimulationManager activitySimulationManager) {
        this.settings = settings;
        this.activitySimulationManager = activitySimulationManager;
        this.name = "activity";
        this.help = "Activity simulation management";
        this.guildOnly = false;
        this.children = new AdminV2Command[]{
                new CreateCommand(),
                new ReadCommand(),
                new SwitchCommand(),
                new DeleteCommand()
        };
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(this, children, name).toString()).setEphemeral(true).queue();
    }

    private class CreateCommand extends AdminV2Command {

        private static final String ACTION_OPTION_KEY = "action";
        private static final String ACTIVITY_OPTION_KEY = "activity";

        CreateCommand() {
            this.name = "create";
            this.help = "Creates new activity rule";
            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, ACTION_OPTION_KEY, "Action in the status").setRequired(true));
            options.add(new OptionData(OptionType.STRING, ACTIVITY_OPTION_KEY, "Activity of the status").setRequired(true));
            this.options = options;
        }

        @Override
        protected final void execute(SlashCommandEvent event) {
            OptionMapping actionOption = event.getOption(ACTION_OPTION_KEY);
            if (actionOption == null) {
                event.replyFormat("%1$s Please specify activity of the status. Supported actions: `[%1$s, %2$s, %3$s]`!",
                        getClient().getWarning(),
                        Const.StatusAction.playing,
                        Const.StatusAction.listening,
                        Const.StatusAction.watching
                ).setEphemeral(true).queue();

                return;
            }

            OptionMapping activityOption = event.getOption(ACTIVITY_OPTION_KEY);
            if (activityOption == null) {
                event.replyFormat("%1$s Status description should not be empty!",
                        getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            Const.StatusAction action;
            String actionOptionString = actionOption.getAsString();

            if (actionOptionString.toLowerCase().startsWith(Const.StatusAction.playing.name())) {
                action = Const.StatusAction.playing;
            } else if (actionOptionString.toLowerCase().startsWith(Const.StatusAction.listening.name())) {
                action = Const.StatusAction.listening;
            } else if (actionOptionString.toLowerCase().startsWith(Const.StatusAction.watching.name())) {
                action = Const.StatusAction.watching;
            } else {
                event.replyFormat("%1$s Action must be one of `[%1$s, %2$s, %3$s]`!",
                        getClient().getWarning(),
                        Const.StatusAction.playing,
                        Const.StatusAction.listening,
                        Const.StatusAction.watching
                ).setEphemeral(true).queue();

                return;
            }

            Activity newPair = new Activity(activityOption.getAsString(), action);
            activitySimulationManager.writeRule(newPair);
            log.info("Added new rule to ActivitySimulation: {}.", newPair);
            event.reply("New rule was added.").setEphemeral(true).queue();
        }
    }

    private class ReadCommand extends AdminV2Command {
        ReadCommand() {
            this.name = "list";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(SlashCommandEvent event) {
            List<Activity> list = activitySimulationManager.getAllRules();
            if (list == null) {
                event.replyFormat("%1$s Failed to load available rules!", getClient().getError()).setEphemeral(true).queue();
            } else if (list.isEmpty()) {
                event.replyFormat("%1$s There are no records available!", getClient().getWarning()).setEphemeral(true).queue();
            } else {
                String message = getClient().getSuccess() + " Acting rules:\r\n";
                StringBuilder builder = new StringBuilder(message);
                list.forEach(rule -> builder.append("`")
                        .append(rule.getId()).append(" ")
                        .append(rule.getStatusAction()).append(" ")
                        .append(rule.getActivityName()).append("`")
                        .append("\r\n"));
                event.reply(builder.toString()).setEphemeral(true).queue();
            }
        }
    }

    private class SwitchCommand extends AdminV2Command {

        private static final String SWITCH_OPTION_KEY = "switch";

        SwitchCommand() {
            this.name = "switch";
            this.help = "Sets or shows simulation of bot's activity";
            this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, SWITCH_OPTION_KEY, "Enable or disable activity simulation").setRequired(false));
        }

        @Override
        protected final void execute(SlashCommandEvent event) {
            boolean currentSetting = settings.get().isSimulateActivity();

            OptionMapping simulateActivityOption = event.getOption(SWITCH_OPTION_KEY);
            if (simulateActivityOption == null) {
                event.replyFormat("Activity simulation is `%1$s`", (currentSetting ? "ON" : "OFF")).setEphemeral(true).queue();

                return;
            }

            boolean newSetting = simulateActivityOption.getAsBoolean();

            if (currentSetting == newSetting) {
                event.replyFormat("Activity simulation is `%1$s`", (currentSetting ? "ON" : "OFF")).setEphemeral(true).queue();

                return;
            }

            settings.get().setSimulateActivity(newSetting);
            if (newSetting) {
                activitySimulationManager.start();
            } else {
                activitySimulationManager.stop();
            }

            event.replyFormat("Activity simulation is `%1$s`", (newSetting ? "ON" : "OFF")).queue();
        }
    }

    private class DeleteCommand extends AdminV2Command {

        private static final String ID_OPTION_KEY = "id";

        DeleteCommand() {
            this.name = "delete";
            this.help = "Deletes an existing activity rule";
            this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, ID_OPTION_KEY, "Id of the rule to be deleted").setRequired(true));
        }

        @Override
        protected final void execute(SlashCommandEvent event) {
            OptionMapping idOption = event.getOption(ID_OPTION_KEY);
            if (idOption == null) {
                event.replyFormat("%1$s Id of command is required", getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            activitySimulationManager.deleteRule(idOption.getAsLong());
            log.info("ActivitySimulation rule with id {} was removed by {}.", idOption.getAsLong(), FormatUtils.formatAuthor(event));
            event.replyFormat("Successfully deleted rule with id `[%1$s]`.", idOption.getAsLong()).setEphemeral(true).queue();
        }
    }
}
