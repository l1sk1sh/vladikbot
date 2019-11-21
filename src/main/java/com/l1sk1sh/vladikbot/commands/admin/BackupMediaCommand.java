package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.services.BackupChannelService;
import com.l1sk1sh.vladikbot.services.BackupMediaService;
import com.l1sk1sh.vladikbot.settings.Constants;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * @author Oliver Johnson
 */
public class BackupMediaCommand extends AdminCommand {
    private final Bot bot;

    public BackupMediaCommand(Bot bot) {
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
        if (bot.isLockedBackup()) {
            event.replyWarning("Can't backup media - another backup is in progress!");
            return;
        }
        event.reply("Getting attachments. Be patient...");

        new Thread(() -> {
            try {
                File exportedTextFile = new BackupChannelService(
                        event.getChannel().getId(),
                        bot.getBotSettings().getToken(),
                        Constants.BACKUP_PLAIN_TEXT,
                        bot.getBotSettings().getLocalPathToExport(),
                        bot.getBotSettings().getDockerPathToExport(),
                        bot.getBotSettings().getDockerContainerName(),
                        event.getArgs().split(" "),
                        bot::setLockedBackup
                ).getExportedFile();

                BackupMediaService backupMediaService = new BackupMediaService(
                        exportedTextFile,
                        event.getChannel().getId(),
                        bot.getBotSettings().getLocalPathToExport(),
                        bot.getBotSettings().getLocalMediaFolder(),
                        String.format("%s - %s [%s] - media list",
                                event.getGuild().getName(),
                                event.getChannel().getName(),
                                event.getChannel().getId()),
                        event.getArgs().split(" "),
                        bot::setLockedBackup
                );

                File exportedMediaUrlsFile = backupMediaService.getMediaUrlsFile();
                if (exportedMediaUrlsFile.length() > Constants.EIGHT_MEGABYTES_IN_BYTES) {
                    // TODO Move such checks into separate Utils or handler
                    event.replyWarning(
                            "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users!\r\n" +
                                    "Limit executed command with period: --before <mm/dd/yy> --after <mm/dd/yy>");
                } else {
                    event.getTextChannel().sendFile(exportedMediaUrlsFile, backupMediaService.getMediaUrlsFile().getName()).queue();

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
    }
}
