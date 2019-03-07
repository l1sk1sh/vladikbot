package com.multiheaded.disbot.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.core.BackupConductor;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

import java.security.InvalidParameterException;

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
    }

    @Override
    public void execute(CommandEvent event) {

        if (!event.isFromType(ChannelType.PRIVATE)) {
            event.reply("Initializing backup process. Be patient...");

            new Thread(() -> {
                try {
                    BackupConductor backupConductor = new BackupConductor(event.getChannel().getId(),
                            event.getArgs().split(" "));

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
}
