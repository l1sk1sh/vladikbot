package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.BackupTextChannelService;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Oliver Johnson
 */
public class BackupTextChannelCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(BackupTextChannelCommand.class);
    private final Bot bot;

    public BackupTextChannelCommand(Bot bot) {
        this.bot = bot;
        this.name = "backup";
        this.help = "creates backup of the current channel\r\n"
                + "\t\t `-b, --before <mm/dd/yyyy>` - specifies date till which backup would be done\r\n"
                + "\t\t `-a, --after  <mm/dd/yyyy>` - specifies date from which backup would be done";
        this.arguments = "-a, -b";
        this.guildOnly = true;
    }

    @Override
    public void execute(CommandEvent event) {
        if (bot.isDockerFailed()) {
            return;
        }

        if (bot.isLockedBackup()) {
            event.replyWarning("Can't perform backup, because another backup is already running!");
            return;
        }
        event.reply("Initializing backup processes. Be patient...");

        BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                bot,
                event.getChannel().getId(),
                Const.BACKUP_HTML_DARK,
                bot.getBotSettings().getLocalTmpFolder(),
                event.getArgs().split(" ")
        );

        /* This thread will wait for backup to finish. Separating allows using bot while backup is running */
        new Thread(() -> {

            /* Creating new thread from service and waiting for it to finish */
            Thread backupChannelServiceThread = new Thread(backupTextChannelService);
            log.info("Starting backupTextChannelService...");
            backupChannelServiceThread.start();
            try {
                backupChannelServiceThread.join();
            } catch (InterruptedException e) {
                log.error("BackupTextChannel was interrupted.", e);
                event.replyError("Backup process was interrupted!");
                return;
            }

            if (backupTextChannelService.hasFailed()) {
                log.error("BackupTextChannelService has failed: {}", backupTextChannelService.getFailMessage());
                event.replyError(String.format("Text channel backup has failed: `[%1$s]`", backupTextChannelService.getFailMessage()));
                return;
            }

            File exportedFile = backupTextChannelService.getBackupFile();
            if (exportedFile.length() > Const.EIGHT_MEGABYTES_IN_BYTES) {
                event.replyWarning(
                        "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users!\r\n" +
                                "Limit executed command with period: --before <mm/dd/yy> --after <mm/dd/yy>");
            } else {
                event.getTextChannel().sendFile(exportedFile, backupTextChannelService.getBackupFile().getName()).queue();
            }
        }).start();
    }
}
