package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.conductors.services.BackupChannelService;
import com.multiheaded.vladikbot.settings.Constants;
import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * @author Oliver Johnson
 */
public class BackupChannelCommand extends AdminCommand {

    public BackupChannelCommand() {
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
                Settings settings = SettingsManager.getInstance().getSettings();
                BackupChannelService service = new BackupChannelService(event.getChannel().getId(), "HtmlDark",
                        event.getArgs().split(" "), settings.getLocalPathToExport(),
                        settings.getDockerPathToExport(), settings.getDockerContainerName(), settings.getToken());

                File exportedFile = service.getExportedFile();
                if (exportedFile.length() > Constants.EIGHT_MEGABYTES_IN_BYTES) {
                    event.replyWarning(
                            "File is too big! Max file-size is 8 MiB for normal and 50 MiB for nitro users!\n" +
                                    "Limit executed command with period: --before <mm/dd/yy> --after <mm/dd/yy>");
                } else {
                    event.getTextChannel().sendFile(exportedFile, service.getExportedFile().getName()).queue();
                }

            } catch (IOException ioe) {
                event.replyWarning("Seems that local storage wasn't prepared. Try again and notify the owner.");
            } catch (InterruptedException ie) {
                event.replyError(String.format("Backup **has failed**! `[%s]`", ie.getMessage()));
            } catch (InvalidParameterException ipe) {
                event.replyError(ipe.getMessage());
            }
        }).start();
    }
}
