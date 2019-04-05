package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.services.AutoModeration;
import com.multiheaded.vladikbot.models.entities.ReactionRule;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
public class AutoModerationCommand extends AdminCommand {
    private final VladikBot bot;
    private final AutoModeration automod;

    public AutoModerationCommand(VladikBot bot) {
        this.bot = bot;
        this.automod = bot.getAutoModeration();
        this.guildOnly = false;
        this.name = "automod";
        this.arguments = "<switch|make|delete|list>";
        this.help = "auto moderation management";
        this.children = new AdminCommand[]{
                new SwitchCommand(),
                new MakeCommand(),
                new DeleteCommand(),
                new ListCommand()
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " AutoModerationService Management Commands:\n");
        for (Command cmd : this.children) {
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments()
                    == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        }
        event.reply(builder.toString());
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
                            bot.getSettings().setAutoModeration(true);
                            break;
                        case "off":
                        case "disable":
                            bot.getSettings().setAutoModeration(false);
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    class MakeCommand extends AdminCommand {

        MakeCommand() {
            this.name = "make";
            this.aliases = new String[]{"create", "add"};
            this.help = "makes a new moderation rule (';' - used as separator)\n" +
                    "Example: **automod add gay_check {gay; гей; мао цзе дун} {кумир; тунец; А чо сразу Валерчик, а?}**";
            this.arguments = "<name> <[react to]> <[react with]>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            try {
                if (!Pattern.compile("^[A-Za-z_]+\\s(\\{(.*?)})\\s(\\{(.*?)})$").matcher(event.getArgs()).matches()) {
                    throw new IllegalArgumentException();
                }

                Set<String> reactTo = new HashSet<>();
                Set<String> reactWith = new HashSet<>();
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
                automod.writeRule(rule);
                event.replySuccess(String.format("Rule was added: `[%s]`", rule.toString()));

            } catch (IllegalArgumentException iae) {
                event.replyWarning(String.format("Input arguments were incorrect `[%s]`", event.getArgs()));
            } catch (IOException ioe) {
                event.replyError(String.format("Failed to write new rule! `[%s]`", ioe.getLocalizedMessage()));
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
            if (automod.getRule(name) == null) {
                event.replyError("Rule `" + name + "` doesn't exist!");
            } else {
                try {
                    automod.deleteRule(name);
                    event.replySuccess(String.format("Successfully deleted rule `%s`!", name));
                } catch (IOException e) {
                    event.replyError(String.format("Unable to delete the rule: `[%s]`", e.getLocalizedMessage()));
                }
            }
        }
    }

    class ListCommand extends AdminCommand {

        ListCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "lists all available rules";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            try {
                Set<ReactionRule> list = bot.getAutoModeration().getRules();
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
                event.replyError(String.format("Local folder couldn't be processed! `[%s]`", ioe.getLocalizedMessage()));
            }
        }
    }
}
