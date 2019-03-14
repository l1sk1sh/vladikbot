package com.multiheaded.disbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.models.conductors.BackupConductor;
import com.multiheaded.disbot.settings.Constants;
import com.multiheaded.disbot.settings.SettingsManager;

import java.io.File;
import java.security.InvalidParameterException;

/**
 * @author Oliver Johnson
 */
public class BackupCommand extends AdminCommand {

    public BackupCommand() {
        this.name = "backup";
        this.help = "creates backup of the current channel\n"
                + "\t\t `-b, --before <mm/dd/yyyy>` - specifies date till which backup would be done\n"
                + "\t\t `-a, --after  <mm/dd/yyyy>` - specifies date from which backup would be done";
        this.arguments = "-a, -b";
        this.guildOnly = true;
    }

    @Override
    public void execute(CommandEvent event) {
        event.reply("Initializing backup processes. Be patient...");

        new Thread(() -> {
            try {
                BackupConductor backupConductor = new BackupConductor(event.getChannel().getId(),
                        event.getArgs().split(" "), SettingsManager.getInstance().getSettings());

                File exportedFile = backupConductor.getBackupService().getExportedFile();
                if (exportedFile.length() > Constants.EIGHT_MEGABYTES_IN_BYTES) {
                    event.replyWarning(
                            "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users!\n" +
                                    "Limit executed command with period: --before mm/dd/yy --after mm/dd/yy");
                } else {
                    event.getTextChannel().sendFile(exportedFile,
                            backupConductor.getBackupService().getExportedFile().getName()).queue();
                }

            } catch (InterruptedException ie) {
                event.replyError(String.format("Backup **has failed**! `[%s]`", ie.getMessage()));
            } catch (InvalidParameterException ipe) {
                event.replyError(ipe.getMessage());
            }
        }).start();
    }
}
