package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.entities.ReactionRule;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
public class AutoModerationCommand extends AdminCommand {
    private final Bot bot;

    public AutoModerationCommand(Bot bot) {
        this.bot = bot;
        this.name = "automod";
        this.help = "auto moderation management";
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
    protected void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " AutoModerationService Management Commands:\r\n");
        for (Command cmd : this.children) {
            builder.append("\r\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments()
                    == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        }
        event.reply(builder.toString());
    }

    class CreateCommand extends AdminCommand {
        CreateCommand() {
            this.name = "make";
            this.aliases = new String[]{"create", "add"};
            this.help = "makes a new moderation rule (';' - used as separator)\r\n" +
                    "Example: *automod add fbi_check {fbi; open up} {POLICE, OPEN UP!; You've played your role, criminal!}*";
            this.arguments = "<name> <{react to}> <{react with}>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            try {
                if (!Pattern.compile("^[A-Za-z_]+\\s(\\{(.*?)})\\s(\\{(.*?)})$").matcher(event.getArgs()).matches()) {
                    throw new IllegalArgumentException();
                }

                List<String> reactTo = new ArrayList<>();
                List<String> reactWith = new ArrayList<>();
                String name = event.getArgs().split(" ", 2)[0];

                Matcher matcher = Pattern.compile("\\{(.*?)}").matcher(event.getArgs());
                int count = 0;
                while (matcher.find()) {
                    count++;
                    String[] array = matcher.group().split(";");
                    for (int i = 0; i < array.length; i++) {
                        array[i] = array[i].trim().replaceAll("[{}]", "");
                    }

                    if (count == 1) Collections.addAll(reactTo, array);
                    if (count == 2) Collections.addAll(reactWith, array);
                }

                ReactionRule rule = new ReactionRule(name, reactTo, reactWith);
                bot.getAutoModerationManager().writeRule(rule);
                event.replySuccess(String.format("Rule was added: `[%1$s]`", rule.toString()));
            } catch (IllegalArgumentException iae) {
                event.replyWarning(String.format("Input arguments were incorrect `[%1$s]`", event.getArgs()));
            } catch (IOException ioe) {
                event.replyError(String.format("Failed to write new rule! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }

    }

    class ReadCommand extends AdminCommand {
        ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "lists all available rules";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            try {
                List<ReactionRule> list = bot.getAutoModerationManager().getRules();
                if (list == null) {
                    event.replyError("Failed to load available rules!");
                } else if (list.isEmpty()) {
                    event.replyWarning("There are no rules in the Rules folder!");
                } else {
                    StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Acting rules:\r\n");
                    list.forEach(str -> builder.append("`").append(str).append("` ").append("\r\n"));
                    event.reply(builder.toString());
                }
            } catch (IOException ioe) {
                event.replyError(String.format("Local folder couldn't be processed! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }
    }

    class DeleteCommand extends AdminCommand {
        DeleteCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing rule";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String name = event.getArgs().replaceAll("\\s+", "_");
            if (bot.getAutoModerationManager().getRule(name) == null) {
                event.replyError(String.format("Rule `%1$s` doesn't exist!", name));
            } else {
                try {
                    bot.getAutoModerationManager().deleteRule(name);
                    event.replySuccess(String.format("Successfully deleted rule `%1$s`!", name));
                } catch (IOException e) {
                    event.replyError(String.format("Unable to delete the rule: `[%1$s]`!", e.getLocalizedMessage()));
                }
            }
        }
    }

    class SwitchCommand extends AdminCommand {
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
            if (args.length > 0) {
                for (String arg : args) {
                    switch (arg) {
                        case "on":
                        case "enable":
                            bot.getBotSettings().setAutoModeration(true);
                            event.replySuccess("AutoModerationManager is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setAutoModeration(false);
                            event.replySuccess("AutoModerationManager is now disabled!");
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }
}
