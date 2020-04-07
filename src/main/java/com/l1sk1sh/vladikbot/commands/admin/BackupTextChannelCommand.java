package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.backup.BackupTextChannelService;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Oliver Johnson
 */
public class BackupTextChannelCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(BackupTextChannelCommand.class);
    private final Bot bot;
    private Const.BackupFileType format;
    private String beforeDate;
    private String afterDate;
    private boolean useExistingBackup;

    public BackupTextChannelCommand(Bot bot) {
        this.bot = bot;
        this.name = "backup";
        this.help = "creates backup of the current channel\r\n"
                + "\t\t `-b, --before <mm/dd/yyyy>` - specifies date till which backup would be done\r\n"
                + "\t\t `-a, --after  <mm/dd/yyyy>` - specifies date from which backup would be done\r\n"
                + "\t\t `--format <csv|html>` - desired format of backup";
        this.arguments = "-a, -b";
        this.guildOnly = true;
        this.format = Const.BackupFileType.HTML_DARK;
        this.beforeDate = null;
        this.afterDate = null;
        this.useExistingBackup = true;
    }

    @Override
    public void execute(CommandEvent event) {
        if (!bot.isDockerRunning()) {
            return;
        }

        if (bot.isLockedBackup()) {
            event.replyWarning("Can't perform backup, because another backup is already running!");
            return;
        }
        event.reply("Initializing backup processes. Be patient...");

        if (!processArguments(event.getArgs().split(" "))) {
            event.replyError(String.format("Failed to processes provided arguments: [%1$s].", event.getArgs()));
        }

        BackupTextChannelService backupTextChannelService = new BackupTextChannelService(
                bot,
                event.getChannel().getId(),
                format,
                bot.getBotSettings().getLocalTmpFolder(),
                beforeDate,
                afterDate,
                useExistingBackup
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
                log.error("BackupTextChannel was interrupted:", e);
                event.replyError("Backup process was interrupted!");
                return;
            }

            if (backupTextChannelService.hasFailed()) {
                log.error("BackupTextChannelService has failed: [{}].", backupTextChannelService.getFailMessage());
                event.replyError(String.format("Text channel backup has failed! `[%1$s]`", backupTextChannelService.getFailMessage()));
                return;
            }

            File exportedFile = backupTextChannelService.getBackupFile();
            CommandUtils.sendFileInMessage(event, exportedFile);
        }).start();
    }

    private boolean processArguments(String... args) {
        if (args.length == 0) {
            return true;
        }

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-b":
                    case "-before":
                        if (CommandUtils.validateBackupDateFormat(args[i + 1])) {
                            beforeDate = (args[i + 1]);
                        } else {
                            return false;
                        }
                        break;
                    case "-a":
                    case "--after":
                        if (CommandUtils.validateBackupDateFormat(args[i + 1])) {
                            afterDate = (args[i + 1]);
                        } else {
                            return false;
                        }
                        break;
                    case "-f":
                    case "--force":

                        /* If force is specified - do not ignore existing files  */
                        useExistingBackup = false;
                        break;
                    case "--format":
                        switch (args[i + 1]) {
                            case "csv":
                                format = Const.BackupFileType.CSV;
                                break;
                            case "html":
                                format = Const.BackupFileType.HTML_LIGHT;
                                break;
                        }
                }
            }
        } catch (IndexOutOfBoundsException iobe) {
            return false;
        }

        /* Check if dates are within correct period (if "before" is more than "after" date) */
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
