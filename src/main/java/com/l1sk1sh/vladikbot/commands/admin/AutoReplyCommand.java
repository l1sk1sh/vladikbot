package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.entity.ReplyRule;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class AutoReplyCommand extends AdminCommand {

    private final AutoReplyManager autoReplyManager;
    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public AutoReplyCommand(GuildSettingsRepository guildSettingsRepository, AutoReplyManager autoReplyManager) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.autoReplyManager = autoReplyManager;
        this.name = "reply";
        this.help = "Auto reply management";
        this.guildOnly = false;
        this.children = new AdminCommand[]{
                new CreateCommand(),
                new ReadCommand(),
                new SwitchCommand(),
                new DeleteCommand(),
                new MatchCommand(),
                new ChanceCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString()).setEphemeral(true).queue();
    }

    private final class CreateCommand extends AdminCommand {

        private static final String REPLY_TO_OPTION_KEY = "action";
        private static final String REPLY_WITH_OPTION_KEY = "activity";

        private static final String OPTION_SEPARATOR = ";";

        private CreateCommand() {
            this.name = "create";
            this.help = "Creates a new reply rule";
            List<OptionData> options = new ArrayList<>();
            options.add(new OptionData(OptionType.STRING, REPLY_TO_OPTION_KEY, "List of words bot will reply to (use " + OPTION_SEPARATOR + " as a separator for multiple phrases at once.").setRequired(true));
            options.add(new OptionData(OptionType.STRING, REPLY_WITH_OPTION_KEY, "List of words bot will reply with (use " + OPTION_SEPARATOR + " as a separator for multiple phrases at once.").setRequired(true));
            this.options = options;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping replyToOption = event.getOption(REPLY_TO_OPTION_KEY);
            if (replyToOption == null) {
                event.replyFormat("%1$s Please enter 'reply to' words.",
                        event.getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            OptionMapping replyWithOption = event.getOption(REPLY_WITH_OPTION_KEY);
            if (replyWithOption == null) {
                event.replyFormat("%1$s Please enter 'reply with' words.",
                        event.getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            List<String> reactTo = Arrays.asList(replyToOption.getAsString().split(OPTION_SEPARATOR));
            List<String> reactWith = Arrays.asList(replyWithOption.getAsString().split(OPTION_SEPARATOR));

            reactTo.removeIf(String::isBlank);
            reactTo.removeIf(str -> str.length() < AutoReplyManager.MIN_REPLY_TO_LENGTH);

            reactWith.removeIf(String::isBlank);
            reactWith.removeIf(str -> str.length() < AutoReplyManager.MIN_REPLY_TO_LENGTH);

            if (reactTo.isEmpty() || reactWith.isEmpty()) {
                event.replyFormat("%1$s Cannot add new reply rule due to empty words (spaces) or words that are less than %2$s character.",
                        event.getClient().getWarning(),
                        AutoReplyManager.MIN_REPLY_TO_LENGTH
                ).setEphemeral(true).queue();

                return;
            }

            ReplyRule rule = new ReplyRule(reactTo, reactWith);
            autoReplyManager.writeRule(rule);
            log.info("Added new reply rule: {}.", rule);
            event.replyFormat("%1$s New rule was added.", event.getClient().getSuccess()).setEphemeral(true).queue();
        }
    }

    private final class ReadCommand extends AdminCommand {
        private static final int MAX_LIST_SIZE_TO_SHOW = 70;

        private ReadCommand() {
            this.name = "list";
            this.help = "Lists all available rules";
            this.guildOnly = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            List<ReplyRule> list = autoReplyManager.getAllRules();

            if (list == null) {
                event.replyFormat("%1$s Failed to load available rules!", event.getClient().getError()).setEphemeral(true).queue();
            } else if (list.isEmpty()) {
                event.replyFormat("%1$s There are no records available!", event.getClient().getWarning()).setEphemeral(true).queue();
            } else if (list.size() > MAX_LIST_SIZE_TO_SHOW) {
                event.replyFormat("%1$s Dictionary size: %2$s records.", event.getClient().getSuccess(), list.size()).setEphemeral(true).queue();
            } else {
                String message = event.getClient().getSuccess() + " Acting rules:\r\n";
                StringBuilder builder = new StringBuilder(message);
                list.forEach(str -> builder.append("`")
                        .append(str)
                        .append("`")
                        .append("\r\n"));
                event.reply(builder.toString()).setEphemeral(true).queue();
            }
        }
    }

    private final class SwitchCommand extends AdminCommand {

        private static final String SWITCH_OPTION_KEY = "switch";

        private SwitchCommand() {
            this.name = "switch";
            this.help = "Enables or disables automatic moderation";
            this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, SWITCH_OPTION_KEY, "Enable or disable automatic reply").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
            boolean currentSetting = settings.map(GuildSettings::isAutoReply).orElse(false);

            OptionMapping autoReplyOption = event.getOption(SWITCH_OPTION_KEY);
            if (autoReplyOption == null) {
                event.replyFormat("Auto reply is `%1$s`", (currentSetting ? "ON" : "OFF")).setEphemeral(true).queue();

                return;
            }

            boolean newSetting = autoReplyOption.getAsBoolean();

            if (currentSetting == newSetting) {
                event.replyFormat("Auto reply is `%1$s`", (currentSetting ? "ON" : "OFF")).setEphemeral(true).queue();

                return;
            }

            settings.ifPresent((guildSettings) -> {
                guildSettings.setAutoReply(newSetting);
                guildSettingsRepository.save(guildSettings);
            });

            event.replyFormat("Auto reply is `%1$s`", (newSetting ? "ON" : "OFF")).setEphemeral(true).queue();
        }
    }

    private final class DeleteCommand extends AdminCommand {

        private static final String ID_OPTION_KEY = "id";

        private DeleteCommand() {
            this.name = "delete";
            this.help = "Deletes an existing rule";
            this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, ID_OPTION_KEY, "Id of the rule to be deleted").setRequired(true));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            OptionMapping idOption = event.getOption(ID_OPTION_KEY);
            if (idOption == null) {
                event.replyFormat("%1$s Id of command is required", event.getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            long id = idOption.getAsLong();
            ReplyRule rule = autoReplyManager.getRuleById(id);

            if (rule == null) {
                event.replyFormat("Rule `%1$s` doesn't exist!", event.getClient().getWarning(), id).setEphemeral(true).queue();
            } else {
                autoReplyManager.deleteRule(rule);
                log.info("Reply rule with id {} was removed by {}.", idOption.getAsLong(), FormatUtils.formatAuthor(event));
                event.replyFormat("%1$s Successfully deleted rule with id `[%2$s]`.", event.getClient().getSuccess(), idOption.getAsLong()).setEphemeral(true).queue();
            }
        }
    }

    private final class MatchCommand extends AdminCommand {

        private static final String MATCH_STRATEGY_OPTION_KEY = "match";

        private MatchCommand() {
            this.name = "match";
            this.help = "Either bot uses full message comparison of word by word";
            options.add(new OptionData(OptionType.STRING, MATCH_STRATEGY_OPTION_KEY, "Reply mathcing strategy").setRequired(false)
                    .addChoice("Match using full sentence", "full")
                    .addChoice("Matching inline", "inline")
            );
            this.guildOnly = false;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
            AutoReplyManager.MatchingStrategy currentStrategy = settings.map(GuildSettings::getMatchingStrategy).orElse(GuildSettings.DEFAULT_MATCHING_STRATEGY);

            OptionMapping matchOption = event.getOption(MATCH_STRATEGY_OPTION_KEY);
            if (matchOption == null) {
                event.replyFormat("Current news style is `%1$s`", currentStrategy).setEphemeral(true).queue();

                return;
            }

            String matchingStrategy = matchOption.getAsString();
            AutoReplyManager.MatchingStrategy strategy;
            try {
                strategy = AutoReplyManager.MatchingStrategy.valueOf(matchingStrategy.toUpperCase());
            } catch (IllegalArgumentException e) {
                event.replyFormat("%1$s Specify either `full` or `inline` strategy.", event.getClient().getWarning()).setEphemeral(true).queue();

                return;
            }

            settings.ifPresent((guildSettings -> {
                guildSettings.setMatchingStrategy(strategy);
                guildSettingsRepository.save(guildSettings);
            }));

            log.info("Matching strategy is set to '{}' by '{}'.", strategy, FormatUtils.formatAuthor(event));
            event.replyFormat("%1$s Changed current matching strategy to `%2$s`.", event.getClient().getSuccess(), strategy.name().toLowerCase()).queue();
        }
    }

    private final class ChanceCommand extends AdminCommand {

        private static final String CHANCE_OPTION_KEY = "chance";

        private ChanceCommand() {
            this.name = "chance";
            this.help = "Chance that bot will reply to your message. ";
            options.add(new OptionData(OptionType.STRING, CHANCE_OPTION_KEY, "1.0 - always replies, 0.0 - never replies").setRequired(true));
            this.guildOnly = false;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());

            OptionMapping chanceOption = event.getOption(CHANCE_OPTION_KEY);
            if (chanceOption == null) {
                event.replyFormat("%1$s Reply chance should not be empty!",
                        event.getClient().getWarning()
                ).setEphemeral(true).queue();

                return;
            }

            try {
                double replyChance = Double.parseDouble(chanceOption.getAsString());
                if (replyChance < 0.0 || replyChance > 1.0) {
                    event.replyFormat("%1$s Reply chance should be in range [0.0 - 1.0]. `%2$s` given", event.getClient().getWarning(), replyChance).setEphemeral(true).queue();

                    return;
                }

                settings.ifPresent((guildSettings) -> {
                    guildSettings.setReplyChance(replyChance);
                    guildSettingsRepository.save(guildSettings);
                });

                event.replyFormat("%1$s Reply chance is now %2$s%%", event.getClient().getSuccess(), replyChance * 100).queue();
            } catch (NumberFormatException nfe) {
                event.replyFormat("%1$s Invalid number specified `[%2$s]`", event.getClient().getWarning(), chanceOption.getAsString()).setEphemeral(true).queue();
            }
        }
    }
}
