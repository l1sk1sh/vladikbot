package com.multiheaded.disbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.models.conductors.BackupConductor;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

import java.security.InvalidParameterException;
import java.util.Set;

/**
 * @author Oliver Johnson
 */
public class BackupCommand extends Command {

    public BackupCommand() {
        this.name = "backup";
        this.help = "creates backup of the current channel\n"
                + "\t\t `-b, --before <mm/dd/yyyy>` - specifies date till which backup would be done\n"
                + "\t\t `-a, --after  <mm/dd/yyyy>` - specifies date from which backup would be done";
        this.arguments = "-a, -b";
        this.requiredRole = "furer";
        this.botPermissions = new Permission[]{Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY,
                Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES};
        this.guildOnly = true;
    }

    @Override
    public void execute(CommandEvent event) {
        event.reply("Initializing backup processes. Be patient...");

        new Thread(() -> {
            try {
                BackupConductor backupConductor = new BackupConductor(event.getChannel().getId(),
                        event.getArgs().split(" "), SettingsManager.getInstance().getSettings());

                event.getTextChannel().sendFile(backupConductor.getBackupService().getExportedFile(),
                        backupConductor.getBackupService().getExportedFile().getName()).queue();
            } catch (InterruptedException ie) {
                event.replyError(String.format("Backup **has failed**! `[%s]`", ie.getMessage()));
            } catch (InvalidParameterException ipe) {
                event.replyError(ipe.getMessage());
            }
        }).start();
    }
}
