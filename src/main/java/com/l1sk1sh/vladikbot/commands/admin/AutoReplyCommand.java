package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.entity.ReplyRule;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
@Service
public class AutoReplyCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(AutoReplyCommand.class);

    private final BotSettingsManager settings;
    private final AutoReplyManager autoReplyManager;

    @Autowired
    public AutoReplyCommand(BotSettingsManager settings, AutoReplyManager autoReplyManager) {
        this.settings = settings;
        this.autoReplyManager = autoReplyManager;
        this.name = "reply";
        this.help = "auto reply management";
        this.arguments = "<add|list|switch|delete|match>";
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
    protected void execute(CommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
    }

    private final class CreateCommand extends AdminCommand {
        private CreateCommand() {
            this.name = "make";
            this.aliases = new String[]{"create", "add"};
            this.help = "makes a new reply rule (';' - used as separator)\r\n" +
                    "Example: *reply add {fbi; open up} {POLICE, OPEN UP!; You've played your role, criminal!}*";
            this.arguments = "<{react to}> <{react with}>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (!Pattern.compile("^(\\{(.*?)})\\s(\\{(.*?)})$").matcher(event.getArgs()).matches()) {
                event.replyWarning(String.format("Input arguments `[%1$s]` " +
                        "do not match pattern `{react to this; or to this}` `{react with this; or this}`", event.getArgs()));
                return;
            }

            List<String> reactTo = new ArrayList<>();
            List<String> reactWith = new ArrayList<>();

            Matcher matcher = Pattern.compile("\\{(.*?)}").matcher(event.getArgs());
            int count = 0;
            while (matcher.find()) {
                count++;
                String[] array = matcher.group().split(";");
                for (int i = 0; i < array.length; i++) {
                    array[i] = array[i].trim().replaceAll("[{}]", "").replaceAll("[\"]", "");
                }

                if (count == 1) {
                    Collections.addAll(reactTo, array);
                }

                if (count == 2) {
                    Collections.addAll(reactWith, array);
                }
            }

            for (String replyTo : reactTo) {
                if (replyTo.isEmpty()) {
                    event.replyError("Do not use empty words for the rule!");
                    return;
                }
            }

            for (String replyWith : reactWith) {
                if (replyWith.isEmpty()) {
                    event.replyError("Do not use empty words for the rule!");
                    return;
                } else if (replyWith.length() < AutoReplyManager.MIN_REPLY_TO_LENGTH) {
                    event.replyError("Trigger words must be more or equal 3 symbols!");
                    return;
                }
            }

            ReplyRule rule = new ReplyRule(reactTo, reactWith);
            autoReplyManager.writeRule(rule);
            log.info("Added new reply rule: {}.", rule.toString());
            event.replySuccess(String.format("Reply rule was added: `[%1$s]`", rule.toString()));
        }

    }

    private final class ReadCommand extends AdminCommand {
        private static final int MAX_LIST_SIZE_TO_SHOW = 70;

        private ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "lists all available rules";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {

            List<ReplyRule> list = autoReplyManager.getAllRules();

            if (list == null) {
                event.replyError("Failed to load available rules!");
            } else if (list.isEmpty()) {
                event.replyWarning("There are no rules at the moment! Add new rules with `add` command.");
            } else if (list.size() > MAX_LIST_SIZE_TO_SHOW) {
                event.replyWarning("Current reply dictionary is too huge to be listed. Contact owner for more details.");
            } else {
                String message = event.getClient().getSuccess() + " Acting rules:\r\n";
                StringBuilder builder = new StringBuilder(message);
                list.forEach(str -> builder.append("`").append(str).append("`").append("\r\n"));
                event.reply(builder.toString());
            }
        }
    }

    private final class DeleteCommand extends AdminCommand {
        private DeleteCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing rule";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            int id = Integer.parseInt(event.getArgs().replaceAll("\\s+", "_"));
            ReplyRule rule = autoReplyManager.getRuleById(id);

            if (rule == null) {
                event.replyError(String.format("Rule `%1$s` doesn't exist!", id));
            } else {
                autoReplyManager.deleteRule(rule);
                log.info("Deleted rule {} by {}.", id, FormatUtils.formatAuthor(event));
                event.replySuccess(String.format("Successfully deleted rule `%1$s`!", id));
            }
        }
    }

    private final class SwitchCommand extends AdminCommand {
        private SwitchCommand() {
            this.name = "switch";
            this.aliases = new String[]{"change"};
            this.help = "enables or disables automatic moderation";
            this.arguments = "<on|off>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length == 0) {
                event.replyWarning("Specify `on` or `off` argument for this command!");
                return;
            }

            for (String arg : args) {
                switch (arg) {
                    case "on":
                    case "enable":
                        settings.get().setAutoReply(true);
                        log.info("Auto Reply was enabled by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Auto Reply is now enabled!");
                        break;
                    case "off":
                    case "disable":
                        settings.get().setAutoReply(false);
                        log.info("Auto Reply was disabled by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Auto Reply is now disabled!");
                        break;
                }
            }
        }
    }

    private final class MatchCommand extends AdminCommand {
        private MatchCommand() {
            this.name = "match";
            this.help = "either bot uses full message comparison of word by word";
            this.arguments = "<full|inline>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length == 0) {
                event.replyWarning("Specify `full` or `inline` argument for this command!");
                return;
            }

            for (String arg : args) {
                switch (arg) {
                    case "full":
                        settings.get().setMatchingStrategy(Const.MatchingStrategy.full);
                        log.info("Matching strategy is set to 'full' by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Whole phrase will be used for reply from now!");
                        break;
                    case "inline":
                        settings.get().setMatchingStrategy(Const.MatchingStrategy.inline);
                        log.info("Matching strategy is set to 'inline' by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Every word will be used for reply!");
                        break;
                }
            }
        }
    }

    private final class ChanceCommand extends AdminCommand {
        private ChanceCommand() {
            this.name = "chance";
            this.help = "chance that bot will reply to your message. 1.0 - always replies, 0.0 - never replies";
            this.arguments = "<0.0 - 1.0>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length == 0) {
                event.replyWarning("Specify double value as a chance of reply!");
                return;
            }

            String lastArgument = args[args.length - 1];

            try {
                double replyChance = Double.parseDouble(lastArgument);
                if (replyChance < 0.0 || replyChance > 1.0) {
                    event.replyWarning(String.format("Reply chance should be in range [0.0 - 1.0]. `%1$s` given", replyChance));
                }
                settings.get().setReplyChance(replyChance);
                event.replySuccess(String.format("Reply chance is now %1$s%%", replyChance * 100));
            } catch (NumberFormatException nfe) {
                event.replyError(String.format("Invalid number specified `[%1$s]`", lastArgument));
            }
        }
    }
}
