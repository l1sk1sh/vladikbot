package l1.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import l1.multiheaded.vladikbot.Bot;
import l1.multiheaded.vladikbot.services.BackupChannelService;
import l1.multiheaded.vladikbot.settings.Constants;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * @author Oliver Johnson
 */
public class BackupChannelCommand extends AdminCommand {
    private final Bot bot;

    public BackupChannelCommand(Bot bot) {
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
        if (bot.isLockedBackup()) {
            event.replyWarning("Can't perform backup, because another backup is already running!");
            return;
        }
        event.reply("Initializing backup processes. Be patient...");

        new Thread(() -> {
            try {
                BackupChannelService service = new BackupChannelService(
                        event.getChannel().getId(),
                        bot.getBotSettings().getToken(),
                        Constants.BACKUP_HTML_DARK,
                        bot.getBotSettings().getLocalPathToExport(),
                        bot.getBotSettings().getDockerPathToExport(),
                        bot.getBotSettings().getDockerContainerName(),
                        event.getArgs().split(" "),
                        bot::setLockedBackup
                );

                File exportedFile = service.getExportedFile();
                if (exportedFile.length() > Constants.EIGHT_MEGABYTES_IN_BYTES) {
                    event.replyWarning(
                            "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users!\r\n" +
                                    "Limit executed command with period: --before <mm/dd/yy> --after <mm/dd/yy>");
                } else {
                    event.getTextChannel().sendFile(exportedFile, service.getExportedFile().getName()).queue();
                }

            } catch (IOException ioe) {
                event.replyWarning(String.format("Something with files gone mad! Ask owner for help! `[%1$s]`",
                        ioe.getLocalizedMessage()));
            } catch (InterruptedException ie) {
                event.replyError(String.format("Backup **has failed**! `[%1$s]`", ie.getLocalizedMessage()));
            } catch (InvalidParameterException ipe) {
                event.replyError(ipe.getLocalizedMessage());
            } catch (Exception e) {
                event.replyError(String.format("Crap! Whatever happened, it wasn't expected! `[%1$s]`",
                        e.getLocalizedMessage()));
            }
        }).start();
    }
}
