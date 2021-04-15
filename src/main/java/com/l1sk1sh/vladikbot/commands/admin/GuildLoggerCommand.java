package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 */
@Service
public class GuildLoggerCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(BackupMediaCommand.class);

    private final BotSettingsManager settings;

    @Autowired
    public GuildLoggerCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "logger";
        this.help = "logs deleted/updated messages and avatars of users";
        this.arguments = "<switch>";
        this.guildOnly = false;
        this.children = new AdminCommand[]{
                new SwitchCommand()
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
    }

    private final class SwitchCommand extends AdminCommand {
        private SwitchCommand() {
            this.name = "switch";
            this.aliases = new String[]{"change"};
            this.help = "enables or disables message logging";
            this.arguments = "<on|off>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] args = event.getArgs().split("\\s+");
            if (args.length == 0) {
                event.replyWarning("Specify `on` or `off` argument for this command!");
                return;
            }

            for (String arg : args) {
                switch (arg) {
                    case "on":
                    case "enable":
                        settings.get().setLogGuildChanges(true);
                        log.info("Guild Logging was enabled by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Guild Logging is now enabled!");
                        break;
                    case "off":
                    case "disable":
                        settings.get().setLogGuildChanges(false);
                        log.info("Guild Logging was disabled by '{}'.", event.getAuthor().getName());
                        event.replySuccess("Guild Logging is now disabled!");
                        break;
                }
            }
        }
    }
}
