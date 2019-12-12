package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Johnson
 */
public class AutoBackupCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(AutoBackupCommand.class);
    private final Bot bot;

    public AutoBackupCommand(Bot bot) {
        this.bot = bot;
        this.name = "abackup";
        this.arguments = "<stext|smedia>";
        this.help = "auto backup management";
        this.guildOnly = true;
        this.children = new OwnerCommand[]{
                new SwitchAutoTextBackupCommand(),
                new SwitchAutoMediaBackupCommand(),
                new FullTextBackupCommand(),
                new FullMediaBackupCommand()
        };
    }

    @Override
    protected final void execute(CommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
    }

    class SwitchAutoTextBackupCommand extends OwnerCommand {
        SwitchAutoTextBackupCommand() {
            this.name = "stext";
            this.help = "enables or disables auto backup for text";
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
                            bot.getBotSettings().setAutoTextBackup(true);
                            bot.getAutoTextBackupDaemon().enableExecution();
                            log.info("Auto text backup was enabled by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
                            event.replySuccess("Auto Text Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setAutoTextBackup(false);
                            try {
                                log.info("Auto text backup was disabled by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
                                bot.getAutoTextBackupDaemon().disableExecution();
                                event.replySuccess("Auto Text Backup is now disabled!");
                            } catch (InterruptedException e) {
                                event.replyError(String.format("Failed to disable auto text backup service! [%1$s]", e.getLocalizedMessage()));
                            }
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    class SwitchAutoMediaBackupCommand extends OwnerCommand {
        SwitchAutoMediaBackupCommand() {
            this.name = "smedia";
            this.help = "enables or disables auto backup for media";
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
                            bot.getBotSettings().setAutoMediaBackup(true);
                            bot.getAutoMediaBackupDaemon().enableExecution();
                            log.info("Auto media backup was enabled by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
                            event.replySuccess("Auto Media Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            bot.getBotSettings().setAutoMediaBackup(false);
                            try {
                                bot.getAutoMediaBackupDaemon().disableExecution();
                                log.info("Auto media backup was disabled by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
                                event.replySuccess("Auto Media Backup is now disabled!");
                            } catch (InterruptedException e) {
                                event.replyError(String.format("Failed to disable auto media backup service! [%1$s]", e.getLocalizedMessage()));
                            }
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    class FullTextBackupCommand extends OwnerCommand {
        FullTextBackupCommand() {
            this.name = "ftext";
            this.help = "launches immediate text backup for channels";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            log.info("Full text backup is about to be executed by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
            bot.getAutoTextBackupDaemon().execute();
        }
    }

    class FullMediaBackupCommand extends OwnerCommand {
        FullMediaBackupCommand() {
            this.name = "fmedia";
            this.help = "launches immediate media backup for channels";
            this.guildOnly = true;
        }

        @Override
        protected final void execute(CommandEvent event) {
            log.info("Full media backup is about to be executed by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
            bot.getAutoMediaBackupDaemon().execute();
        }
    }
}
