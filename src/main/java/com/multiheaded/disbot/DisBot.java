package com.multiheaded.disbot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.multiheaded.disbot.commands.everyone.SettingsCommand;
import com.multiheaded.disbot.commands.owner.ShutdownCommand;
import com.multiheaded.disbot.commands.admin.BackupCommand;
import com.multiheaded.disbot.commands.admin.EmojiStatsCommand;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

/**
 * @author Oliver Johnson
 */
public class DisBot {
    private static final Logger logger = LoggerFactory.getLogger(DisBot.class);

    public static void main(String[] args) {
        try {
            Settings settings = SettingsManager.getInstance().getSettings();
            EventWaiter waiter = new EventWaiter();
            Bot bot = new Bot(waiter);

            CommandClientBuilder commandClientBuilder = new CommandClientBuilder()
                    .setPrefix(settings.getPrefix())
                    .setOwnerId(Long.toString(settings.getOwnerId()))
                    .setEmojis(settings.getSuccessEmoji(), settings.getWarningEmoji(), settings.getErrorEmoji())
                    .setHelpWord(settings.getHelpWord())
                    .setStatus((settings.getOnlineStatus() != OnlineStatus.UNKNOWN)
                            ? settings.getOnlineStatus() : OnlineStatus.DO_NOT_DISTURB)
                    .setGame((settings.getGame() != null)
                            ? settings.getGame() : Game.playing("with your mom"))
                    .setLinkedCacheSize(200)
                    .addCommands(
                            new PingCommand(),
                            new SettingsCommand(),

                            new BackupCommand(),
                            new EmojiStatsCommand(waiter),


                            new ShutdownCommand(bot)
                    );

            new JDABuilder(AccountType.BOT)
                    .setToken(settings.getToken())
                    .addEventListener(
                            waiter,
                            commandClientBuilder.build()
                    )
                    .build();
        } catch (ExceptionInInitializerError e) {
            logger.error("Problematic settings input");
        } catch (LoginException le) {
            logger.error("Invalid username and/or password.");
        }
    }
}
