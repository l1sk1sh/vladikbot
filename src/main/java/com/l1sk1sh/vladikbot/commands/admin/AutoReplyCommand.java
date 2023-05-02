package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.entity.ReplyReaction;
import com.l1sk1sh.vladikbot.data.entity.ReplyRule;
import com.l1sk1sh.vladikbot.data.entity.ReplyTrigger;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
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
                new SwitchCommand(),
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

            // TODO Iterate list or remove list at all
            ReplyRule rule = new ReplyRule(new ReplyTrigger(reactTo.get(0)), new ReplyReaction(reactWith.get(0)));
            autoReplyManager.writeRule(rule);
            log.info("Added new reply rule: {}.", rule.toString());
            event.replyFormat("%1$s New rule was added.", event.getClient().getSuccess()).setEphemeral(true).queue();
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
