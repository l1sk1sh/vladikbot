package com.multiheaded.disbot.command;

import com.multiheaded.disbot.util.StringUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import static com.multiheaded.disbot.settings.Constants.BOT_PREFIX;

public class HelpCommand extends AbstractCommand {
    private static final String NO_NAME = "No name provided for this command. Sorry!";
    private static final String NO_DESCRIPTION = "No description has been provided for this command. Sorry!";
    private static final String NO_USAGE = "No usage instructions have been provided for this command. Sorry!";

    private TreeMap<String, AbstractCommand> commands;

    public HelpCommand() {
        commands = new TreeMap<>();
    }

    public AbstractCommand registerCommand(AbstractCommand command) {
        commands.put(command.getAliases().get(0), command);
        return command;
    }

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        if (!e.isFromType(ChannelType.PRIVATE)) {
            e.getTextChannel().sendMessage(new MessageBuilder()
                    .append(e.getAuthor())
                    .append(": Help information was sent as a private message.")
                    .build()).queue();
        }
        sendPrivate(e.getAuthor().openPrivateChannel().complete(), args);
    }

    private void sendPrivate(PrivateChannel channel, String[] args) {
        if (args.length < 2) {
            StringBuilder s = new StringBuilder();

            for (AbstractCommand c : commands.values()) {
                String description = c.getDescription();
                description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;

                s.append("**").append(c.getAliases().get(0)).append("** - ");
                s.append(description).append("\n");
            }

            channel.sendMessage(new MessageBuilder()
                    .append("The following commands are supported by the bot.\n")
                    .append(s.toString())
                    .build()).queue();
        } else {
            String command = (args[1].charAt(0) == BOT_PREFIX.charAt(0)) ? args[1] : BOT_PREFIX + args[1];

            for (AbstractCommand c : commands.values()) {
                if (c.getAliases().contains(command)) {
                    String name = c.getName();
                    String description = c.getDescription();
                    List<String> usageInstructions = c.getUsageInstructions();

                    name = (name == null || name.isEmpty()) ? NO_NAME : name;
                    description = (description == null || description.isEmpty()) ? NO_DESCRIPTION : description;
                    usageInstructions = (usageInstructions == null || usageInstructions.isEmpty()) ?
                            Collections.singletonList(NO_USAGE) : usageInstructions;

                    channel.sendMessage(new MessageBuilder()
                            .append(String.format("**Name:** %s \n", name))
                            .append(String.format("**Description:** %s \n", description))
                            .append(String.format("**Aliases:** %s \n", StringUtils.join(c.getAliases(), ", ")))
                            .append("**Usage:** ")
                            .append(usageInstructions.get(0))
                            .build()).queue();

                    for (int i = 1; i < usageInstructions.size(); i++) {
                        channel.sendMessage(new MessageBuilder()
                                .append(String.format("__%s Usage Cont. (%s)__\n", name, (i + 1)))
                                .append(usageInstructions.get(i))
                                .build()).queue();
                    }
                    return;
                }
            }

            channel.sendMessage(new MessageBuilder()
                    .append(String.format("The provided command '**%s**' does not exist. " +
                            "Use %shelp to list all commands.", args[1], BOT_PREFIX))
                    .build()).queue();
        }
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(BOT_PREFIX + "help", BOT_PREFIX + "commands");
    }

    @Override
    public String getDescription() {
        return "Command that helps to use all other commands.";
    }

    @Override
    public String getName() {
        return "Help Command";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
                BOT_PREFIX + "help  **OR**  " + BOT_PREFIX + "help *<command>*\n"
                        + BOT_PREFIX + "help - returns the list of commands along with a simple description of each.\n"
                        + BOT_PREFIX + "help <command> - returns the name, description, aliases and usage information of a command.");
    }
}
