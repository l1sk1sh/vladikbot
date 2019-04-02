package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.conductors.BackupMediaConductor;
import com.multiheaded.vladikbot.conductors.services.BackupMediaService;
import com.multiheaded.vladikbot.settings.Constants;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * @author Oliver Johnson
 */
public class BackupMediaCommand extends AdminCommand {

    public BackupMediaCommand() {
        this.name = "savemedia";
        this.help = "exports all attachments of the current channel\n"
                + "\t\t `-b, --before <mm/dd/yyyy>` - specifies date till which export would be done\n"
                + "\t\t `-a, --after  <mm/dd/yyyy>` - specifies date from which export would be done\n"
                + "\t\t `-f` - creates new backup ignoring existing files.\n"
                + "\t\t `-a, --all` - backups all attachments. By default only .jpg, .png and .mp4\n"
                + "\t\t `-z, --zip` - zip flag that creates local copy of files from media links.";
        this.arguments = "-a, -b, -f, -a, -z";
        this.guildOnly = true;
    }

    @Override
    public void execute(CommandEvent event) {
        event.reply("Getting attachments. Be patient...");

        new Thread(() -> {
            try {
                String fileName = String.format("%s - %s [%s] - media list",
                        event.getGuild().getName(),
                        event.getChannel().getName(),
                        event.getChannel().getId());

                BackupMediaConductor backupMediaConductor =
                        new BackupMediaConductor(event.getChannel().getId(),
                                fileName,
                                event.getArgs().split(" "));
                BackupMediaService service = backupMediaConductor.getBackupMediaService();

                File exportedFile = service.getTxtMediaSet();
                if (exportedFile.length() > Constants.EIGHT_MEGABYTES_IN_BYTES) {
                    event.replyWarning(
                            "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users!\n" +
                                    "Limit executed command with period: --before <mm/dd/yy> --after <mm/dd/yy>");
                } else {
                    event.getTextChannel().sendFile(exportedFile, service.getTxtMediaSet().getName()).queue();

                    if (service.doZip() && service.isDownloadComplete()) {
                        event.replySuccess("Zip with uploaded media files could be downloaded from local storage.");
                    }
                }

            } catch (InterruptedException e) {
                event.replyError(String.format("Backup **has failed**! `[%s]`", e.getMessage()));
            } catch (IOException ioe) {
                event.replyError(String.format("**Failed** to properly *work* with files! `[%s]`", ioe.getMessage()));
            } catch (InvalidParameterException ipe) {
                event.replyError(ipe.getMessage());
            }
        }).start();
    }
}