package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Johnson
 */
public class RotatingBackupCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(RotatingBackupCommand.class);
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
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
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
                            bot.getRotatingTextBackupDaemon().enableExecution();
                            event.replySuccess("Rotating Text Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setRotateTextBackup(false);
                            try {
                                bot.getRotatingTextBackupDaemon().disableExecution();
                                event.replySuccess("Rotating Text Backup is now disabled!");
                            } catch (InterruptedException e) {
                                event.replyError(String.format("Failed to disable text rotation service! [%1$s]", e.getLocalizedMessage()));
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
                            bot.getRotatingMediaBackupDaemon().enableExecution();
                            log.info("Rotation backup was enabled by {}:[{}]", event.getAuthor().getName(), event.getAuthor().getId());
                            event.replySuccess("Rotating Media Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setRotateMediaBackup(false);
                            try {
                                bot.getRotatingMediaBackupDaemon().disableExecution();
                                log.info("Rotation backup was disabled by {}:[{}]", event.getAuthor().getName(), event.getAuthor().getId());
                                event.replySuccess("Rotating Media Backup is now disabled!");
                            } catch (InterruptedException e) {
                                event.replyError(String.format("Failed to disable media rotation service! [%1$s]", e.getLocalizedMessage()));
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
            log.info("Full text backup is about to be executed by {}:[{}]", event.getAuthor().getName(), event.getAuthor().getId());
            bot.getRotatingTextBackupDaemon().execute();
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
            log.info("Full media backup is about to be executed by {}:[{}]", event.getAuthor().getName(), event.getAuthor().getId());
            bot.getRotatingMediaBackupDaemon().execute();
        }
    }
}
