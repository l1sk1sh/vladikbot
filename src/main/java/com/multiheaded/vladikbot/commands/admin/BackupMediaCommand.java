package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.services.BackupChannelService;
import com.multiheaded.vladikbot.services.BackupMediaService;
import com.multiheaded.vladikbot.settings.Constants;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * @author Oliver Johnson
 */
public class BackupMediaCommand extends AdminCommand {
    private final VladikBot bot;

    public BackupMediaCommand(VladikBot bot) {
        this.bot = bot;
        this.name = "savemedia";
        this.help = "exports all attachments of the current channel\r\n"
                + "\t\t `-b, --before <mm/dd/yyyy>` - specifies date till which export would be done\r\n"
                + "\t\t `-a, --after  <mm/dd/yyyy>` - specifies date from which export would be done\r\n"
                + "\t\t `-f` - creates new backup ignoring existing files\r\n"
                + "\t\t `-a, --all` - backups all attachments. By default only .jpg, .png and .mp4\r\n"
                + "\t\t `-z, --zip` - zip flag that creates local copy of files from media links";
        this.arguments = "-a, -b, -f, -a, -z";
        this.guildOnly = true;
    }

    @Override
    public void execute(CommandEvent event) {
        if (bot.isBackupAvailable()) {
            event.reply("Getting attachments. Be patient...");

            new Thread(() -> {
                try {
                    BackupMediaService backupMediaService = new BackupMediaService(
                            new BackupChannelService(
                                    event.getChannel().getId(),
                                    bot.getSettings().getToken(),
                                    Constants.BACKUP_PLAIN_TEXT,
                                    bot.getSettings().getLocalPathToExport(),
                                    bot.getSettings().getDockerPathToExport(),
                                    bot.getSettings().getDockerContainerName(),
                                    event.getArgs().split(" "),
                                    bot::setAvailableBackup
                            ).getExportedFile(),
                            event.getChannel().getId(),
                            bot.getSettings().getLocalPathToExport(),
                            bot.getSettings().getLocalMediaFolder(),
                            String.format("%s - %s [%s] - media list",
                                    event.getGuild().getName(),
                                    event.getChannel().getName(),
                                    event.getChannel().getId()),
                            event.getArgs().split(" "),
                            bot::setAvailableBackup
                    );

                    File exportedFile = backupMediaService.getMediaUrlsFile();
                    if (exportedFile.length() > Constants.EIGHT_MEGABYTES_IN_BYTES) {
                        event.replyWarning(
                                "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users!\r\n" +
                                        "Limit executed command with period: --before <mm/dd/yy> --after <mm/dd/yy>");
                    } else {
                        event.getTextChannel().sendFile(exportedFile, backupMediaService.getMediaUrlsFile().getName()).queue();

                        if (backupMediaService.doZip() && backupMediaService.isDownloadComplete()) {
                            event.replySuccess("Zip with uploaded media files could be downloaded from local storage.");
                        }
                    }

                } catch (InterruptedException e) {
                    event.replyError(String.format("Backup **has failed**! `[%1$s]`", e.getLocalizedMessage()));
                } catch (IOException ioe) {
                    event.replyError(String.format("**Failed** to properly *work* with files! `[%1$s]`", ioe.getLocalizedMessage()));
                } catch (InvalidParameterException ipe) {
                    event.replyError(ipe.getLocalizedMessage());
                } catch (Exception e) {
                    event.replyError(String.format("Crap! Whatever happened, it wasn't expected! `[%1$s]`", e.getLocalizedMessage()));
                }
            }).start();
        } else {
            event.replyWarning("Can't backup media - another backup is in progress!");
        }
    }
}
