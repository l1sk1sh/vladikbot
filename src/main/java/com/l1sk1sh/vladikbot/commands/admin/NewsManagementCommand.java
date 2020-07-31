package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Johnson
 */
public class NewsManagementCommand extends AdminCommand {
    private static final Logger log = LoggerFactory.getLogger(NewsManagementCommand.class);
    private final Bot bot;

    public NewsManagementCommand(Bot bot) {
        this.bot = bot;
        this.name = "news";
        this.help = "Turn news update on or off";
        this.arguments = "<on|off>";
    }

    @Override
    protected final void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+");
        if (args.length > 0) {
            for (String arg : args) {
                switch (arg) {
                    case "on":
                    case "enable":
                        bot.getBotSettings().setSendNews(true);
                        event.replySuccess("News feed is now enabled!");
                        bot.getRssService().start();
                        // Add another news services here
                        break;
                    case "off":
                    case "disable":
                        bot.getBotSettings().setSendNews(false);
                        event.replySuccess("News feed is now disabled!");
                        bot.getRssService().stop();
                        // Add another news services here
                        break;
                }
            }
        } else {
            event.replyWarning("Specify `on` or `off` argument for this command!");
        }
    }
}
