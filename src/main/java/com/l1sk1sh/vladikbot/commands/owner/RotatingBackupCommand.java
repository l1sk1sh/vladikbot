package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;

/**
 * @author Oliver Johnson
 */
public class RotatingBackupCommand extends OwnerCommand {
    private final Bot bot;

    public RotatingBackupCommand(Bot bot) {
        this.bot = bot;
        this.name = "rbackup";
        this.arguments = "<stext|smedia>";
        this.help = "rotating backup management";
        this.guildOnly = true;
        this.children = new OwnerCommand[]{
                new RotatingBackupCommand.SwitchTextCommand(),
                new RotatingBackupCommand.SwitchMediaCommand(),
                new FullTextBackup(),
                new FullMediaBackup()
        };
    }

    @Override
    protected final void execute(CommandEvent event) {
        String message = event.getClient().getWarning() + " Backup Rotation Management Commands:\r\n";
        StringBuilder builder = new StringBuilder(message);
        for (Command cmd : this.children) {
            builder.append("\r\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments()
                    == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        }
        event.reply(builder.toString());
    }

    class SwitchTextCommand extends OwnerCommand {
        SwitchTextCommand() {
            this.name = "stext";
            this.help = "enables or disables rotating backup for text";
            this.arguments = "<on|off>";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length > 0) {
                for (String arg : args) {
                    switch (arg) {
                        case "on":
                        case "enable":
                            bot.getBotSettings().setRotateTextBackup(true);
                            bot.getRotatingBackupChannelService().enableExecution();
                            event.replySuccess("Rotating Text Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setRotateTextBackup(false);
                            try {
                                bot.getRotatingBackupChannelService().disableExecution();
                                event.replySuccess("Rotating Text Backup is now disabled!");
                            } catch (InterruptedException e) {
                                event.replyError(String.format("Failed to disable text rotation service! [%s]", e.getLocalizedMessage()));
                            }
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    class SwitchMediaCommand extends OwnerCommand {
        SwitchMediaCommand() {
            this.name = "smedia";
            this.help = "enables or disables rotating backup for media";
            this.arguments = "<on|off>";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length > 0) {
                for (String arg : args) {
                    switch (arg) {
                        case "on":
                        case "enable":
                            bot.getBotSettings().setRotateMediaBackup(true);
                            bot.getRotatingBackupMediaService().enableExecution();
                            event.replySuccess("Rotating Media Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setRotateMediaBackup(false);
                            try {
                                bot.getRotatingBackupMediaService().disableExecution();
                                event.replySuccess("Rotating Media Backup is now disabled!");
                            } catch (InterruptedException e) {
                                event.replyError(String.format("Failed to disable media rotation service! [%s]", e.getLocalizedMessage()));
                            }
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    class FullTextBackup extends OwnerCommand {
        FullTextBackup() {
            this.name = "ftext";
            this.help = "launches immediate force text backup for channels";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            bot.getRotatingBackupChannelService().execute();
        }
    }

    class FullMediaBackup extends OwnerCommand {
        FullMediaBackup() {
            this.name = "fmedia";
            this.help = "launches immediate force media backup for channels";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            bot.getRotatingBackupMediaService().execute();
        }
    }
}
