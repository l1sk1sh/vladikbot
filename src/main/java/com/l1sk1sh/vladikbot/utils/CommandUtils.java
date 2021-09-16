package com.l1sk1sh.vladikbot.utils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.settings.Const;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author l1sk1sh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandUtils {

    public static StringBuilder getListOfChildCommands(CommandEvent event, Command[] children, String nameOfParentCommand) {
        String message = event.getClient().getWarning() + " " + nameOfParentCommand + " has following commands:\r\n";
        return getListOfCommands(children, nameOfParentCommand, message, event.getClient());
    }

    public static StringBuilder getListOfChildCommands(SlashCommand command, Command[] children, String nameOfParentCommand) {
        String message = command.getClient() + " " + nameOfParentCommand + " has following commands:\r\n";
        return getListOfCommands(children, nameOfParentCommand, message, command.getClient());
    }

    @NotNull
    private static StringBuilder getListOfCommands(Command[] children, String nameOfParentCommand, String message, CommandClient client) {
        StringBuilder builder = new StringBuilder(message);
        for (Command cmd : children) {
            builder.append("\r\n`")
                    .append(client.getPrefix())
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

    /**
     * Check if dates are within correct period (if "before" is more than "after" date)
     *
     * @param beforeDate end of the period
     * @param afterDate  start of the period
     * @return true, if period is valid
     */
    public static boolean validatePeriod(String beforeDate, String afterDate) {
        if (beforeDate != null && afterDate != null) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

                Date before = simpleDateFormat.parse(beforeDate);
                Date after = simpleDateFormat.parse(afterDate);

                if (before.compareTo(after) < 0 || before.compareTo(after) == 0) {
                    return false;
                }
            } catch (ParseException e) {
                return false;
            }
        }

        return true;
    }
}
