package com.l1sk1sh.vladikbot.utils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public final class CommandUtils {
    private CommandUtils() {}

    public static StringBuilder getListOfChildCommands(CommandEvent event, Command[] children, String nameOfParentCommand) {
        String message = event.getClient().getWarning() + " " + nameOfParentCommand + " has following commands:\r\n";
        StringBuilder builder = new StringBuilder(message);
        for (Command cmd : children) {
            builder.append("\r\n`")
                    .append(event.getClient().getPrefix())
                    .append(nameOfParentCommand)
                    .append(" ")
                    .append(cmd.getName())
                    .append(" ")
                    .append(cmd.getArguments() == null
                            ? ""
                            : cmd.getArguments())
                    .append("` - ")
                    .append(cmd.getHelp());
        }
        return builder;
    }
}
