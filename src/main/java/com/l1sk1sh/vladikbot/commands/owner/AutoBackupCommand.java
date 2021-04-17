package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.backup.AutoMediaBackupDaemon;
import com.l1sk1sh.vladikbot.services.backup.AutoTextBackupDaemon;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 */
@Service
public class AutoBackupCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(AutoBackupCommand.class);

    @Qualifier("backupThreadPool")
    private final ScheduledExecutorService backupThreadPool;
    private final BotSettingsManager settings;
    private final AutoTextBackupDaemon autoTextBackupDaemon;
    private final AutoMediaBackupDaemon autoMediaBackupDaemon;

    @Autowired
    public AutoBackupCommand(ScheduledExecutorService backupThreadPool,
                             BotSettingsManager settings, AutoTextBackupDaemon autoTextBackupDaemon, AutoMediaBackupDaemon autoMediaBackupDaemon) {
        this.backupThreadPool = backupThreadPool;
        this.settings = settings;
        this.autoTextBackupDaemon = autoTextBackupDaemon;
        this.autoMediaBackupDaemon = autoMediaBackupDaemon;
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

    private final class SwitchAutoTextBackupCommand extends OwnerCommand {
        private SwitchAutoTextBackupCommand() {
            this.name = "stext";
            this.help = "enables or disables auto backup for text";
            this.arguments = "<on|off>";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length > 0) {
                for (String arg : args) {
                    switch (arg) {
                        case "on":
                        case "enable":
                            if (settings.get().isAutoTextBackup()) {
                                log.info("Auto text backup is already enabled.");
                                break;
                            }

                            settings.get().setAutoTextBackup(true);
                            autoTextBackupDaemon.start();
                            log.info("Auto text backup was enabled by {}.", FormatUtils.formatAuthor(event));
                            event.replySuccess("Auto Text Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            if (!settings.get().isAutoTextBackup()) {
                                log.info("Auto text backup is already disabled.");
                                break;
                            }

                            settings.get().setAutoTextBackup(false);
                            log.info("Auto text backup was disabled by {}.", FormatUtils.formatAuthor(event));
                            autoTextBackupDaemon.stop();
                            event.replySuccess("Auto Text Backup is now disabled!");
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    private final class SwitchAutoMediaBackupCommand extends OwnerCommand {
        private SwitchAutoMediaBackupCommand() {
            this.name = "smedia";
            this.help = "enables or disables auto backup for media";
            this.arguments = "<on|off>";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length > 0) {
                for (String arg : args) {
                    switch (arg) {
                        case "on":
                        case "enable":
                            if (settings.get().isAutoMediaBackup()) {
                                log.info("Auto media backup is already enabled.");
                                break;
                            }

                            settings.get().setAutoMediaBackup(true);
                            autoMediaBackupDaemon.start();
                            log.info("Auto media backup was enabled by {}.", FormatUtils.formatAuthor(event));
                            event.replySuccess("Auto Media Backup is now enabled!");
                            break;
                        case "off":
                        case "disable":
                            if (!settings.get().isAutoMediaBackup()) {
                                log.info("Auto media backup is already disabled.");
                                break;
                            }

                            settings.get().setAutoMediaBackup(false);
                            autoMediaBackupDaemon.stop();
                            log.info("Auto media backup was disabled by {}.", FormatUtils.formatAuthor(event));
                            event.replySuccess("Auto Media Backup is now disabled!");
                            break;
                    }
                }
            } else {
                event.replyWarning("Specify `on` or `off` argument for this command!");
            }
        }
    }

    private final class FullTextBackupCommand extends OwnerCommand {
        private FullTextBackupCommand() {
            this.name = "ftext";
            this.help = "launches immediate text backup for channels";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            log.info("Full text backup is about to be executed by {}.", FormatUtils.formatAuthor(event));
            backupThreadPool.execute((autoTextBackupDaemon::execute));
            event.reply("Launched full text backup using daemon.");
        }
    }

    private final class FullMediaBackupCommand extends OwnerCommand {
        private FullMediaBackupCommand() {
            this.name = "fmedia";
            this.help = "launches immediate media backup for channels";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            log.info("Full media backup is about to be executed by {}.", FormatUtils.formatAuthor(event));
            backupThreadPool.execute(autoMediaBackupDaemon::execute);
            event.reply("Launched full media backup using daemon.");
        }
    }
}
