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
                            if (bot.getBotSettings().shouldAutoTextBackup()) {
                                log.info("Auto text backup is already enabled.");
                                break;
                            }

                            bot.getBotSettings().setAutoTextBackup(true);
                            bot.getAutoTextBackupDaemon().start();
                            log.info("Auto text backup was enabled by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
                            event.replySuccess("Auto Text Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            if (!bot.getBotSettings().shouldAutoTextBackup()) {
                                log.info("Auto text backup is already disabled.");
                                break;
                            }

                            bot.getBotSettings().setAutoTextBackup(false);
                            log.info("Auto text backup was disabled by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
                            bot.getAutoTextBackupDaemon().stop();
                            event.replySuccess("Auto Text Backup is now disabled!");
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
                            if (bot.getBotSettings().shouldAutoMediaBackup()) {
                                log.info("Auto media backup is already enabled.");
                                break;
                            }

                            bot.getBotSettings().setAutoMediaBackup(true);
                            bot.getAutoMediaBackupDaemon().start();
                            log.info("Auto media backup was enabled by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
                            event.replySuccess("Auto Media Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            if (!bot.getBotSettings().shouldAutoMediaBackup()) {
                                log.info("Auto media backup is already disabled.");
                                break;
                            }

                            bot.getBotSettings().setAutoMediaBackup(false);
                            bot.getAutoMediaBackupDaemon().stop();
                            log.info("Auto media backup was disabled by {}:[{}].", event.getAuthor().getName(), event.getAuthor().getId());
                            event.replySuccess("Auto Media Backup is now disabled!");
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
            bot.getBackThreadPool().execute((() -> bot.getAutoTextBackupDaemon().execute()));
            event.reply("Launched full text backup using daemon.");
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
            bot.getBackThreadPool().execute((() -> bot.getAutoMediaBackupDaemon().execute()));
            event.reply("Launched full media backup using daemon.");
        }
    }
}
