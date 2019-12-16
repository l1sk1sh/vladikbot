package com.l1sk1sh.vladikbot.utils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.Const;

import java.io.File;

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

    public static void sendFileInMessage(CommandEvent event, File exportedFile) {
        if (exportedFile.length() > Const.EIGHT_MEGABYTES_IN_BYTES) {
            event.replyWarning(
                    "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users!\r\n" +
                            "Limit executed command with period: --before <mm/dd/yy> --after <mm/dd/yy>");
        } else {
            event.getTextChannel().sendFile(exportedFile, exportedFile.getName()).queue();
        }
    }

    public static boolean validateBackupDateFormat(String date) {
        return date.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})");
    }
}
