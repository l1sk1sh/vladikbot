package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.entities.ReplyRule;
import com.l1sk1sh.vladikbot.services.AutoReplyManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
public class AutoReplyCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(AutoReplyCommand.class);

    private final Bot bot;

    public AutoReplyCommand(Bot bot) {
        this.bot = bot;
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
        CreateCommand() {
            this.name = "make";
            this.aliases = new String[]{"create", "add"};
            this.help = "makes a new reply rule (';' - used as separator)\r\n" +
                    "Example: *automod add {fbi; open up} {POLICE, OPEN UP!; You've played your role, criminal!}*";
            this.arguments = "<{react to}> <{react with}>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            try {
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
                bot.getAutoReplyManager().writeRule(rule);
                log.info("Added new reply rule: {}.", rule.toString());
                event.replySuccess(String.format("Reply rule was added: `[%1$s]`", rule.toString()));
            } catch (IOException ioe) {
                log.error("IO error during addition of new reply rule execution:", ioe);
                event.replyError(String.format("Failed to write new reply rule! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }

    }

    private final class ReadCommand extends AdminCommand {
        ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "lists all available rules";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            try {
                List<ReplyRule> list = bot.getAutoReplyManager().getAllRules();

                if (list == null) {
                    event.replyError("Failed to load available rules!");
                } else if (list.isEmpty()) {
                    event.replyWarning("There are no rules at the moment! Add new rules with `add` command.");
                } else {
                    String message = event.getClient().getSuccess() + " Acting rules:\r\n";
                    StringBuilder builder = new StringBuilder(message);
                    list.forEach(str -> builder.append("`").append(str).append("`").append("\r\n"));
                    event.reply(builder.toString());
                }
            } catch (IOException ioe) {
                log.error("IO error during reading of existing reply rules execution:", ioe);
                event.replyError(String.format("Local folder couldn't be processed! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }
    }

    private final class DeleteCommand extends AdminCommand {
        DeleteCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing rule";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            int id = Integer.parseInt(event.getArgs().replaceAll("\\s+", "_"));
            if (bot.getAutoReplyManager().getRuleById(id) == null) {
                event.replyError(String.format("Rule `%1$s` doesn't exist!", id));
            } else {
                try {
                    bot.getAutoReplyManager().deleteRule(id);
                    log.info("Deleted rule {} by {}[{}].", id, event.getAuthor().getName(), event.getAuthor().getId());
                    event.replySuccess(String.format("Successfully deleted rule `%1$s`!", id));
                } catch (IOException e) {
                    log.error("Failed to delete moderation rule by id '{}'.", id, e);
                    event.replyError(String.format("Unable to delete the rule: `[%1$s]`!", e.getLocalizedMessage()));
                }
            }
        }
    }

    private final class SwitchCommand extends AdminCommand {
        SwitchCommand() {
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
                        bot.getBotSettings().setAutoReply(true);
                        log.info("Auto Reply was enabled by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Auto Reply is now enabled!");
                        break;
                    case "off":
                    case "disable":
                        bot.getBotSettings().setAutoReply(false);
                        log.info("Auto Reply was disabled by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Auto Reply is now disabled!");
                        break;
                }
            }
        }
    }

    private final class MatchCommand extends AdminCommand {
        MatchCommand() {
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
                        bot.getBotSettings().setMatchingStrategy(Const.MatchingStrategy.full);
                        log.info("Matching strategy is set to 'full' by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Whole phrase will be used for reply from now!");
                        break;
                    case "inline":
                        bot.getBotSettings().setMatchingStrategy(Const.MatchingStrategy.inline);
                        log.info("Matching strategy is set to 'inline' by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Every word will be used for reply!");
                        break;
                }
            }
        }
    }

    private final class ChanceCommand extends AdminCommand {
        ChanceCommand() {
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
                bot.getBotSettings().setReplyChange(replyChance);
                event.replySuccess(String.format("Reply chance is now %1$s%%", replyChance * 100));
            } catch (NumberFormatException nfe) {
                event.replyError(String.format("Invalid number specified `[%1$s]`", lastArgument));
            }
        }
    }
}
