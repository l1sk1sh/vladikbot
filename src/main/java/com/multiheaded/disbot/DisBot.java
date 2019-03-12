package com.multiheaded.disbot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.jagrosh.jdautilities.examples.command.ShutdownCommand;
import com.multiheaded.disbot.command.BackupCommand;
import com.multiheaded.disbot.command.EmojiStatsCommand;
import com.multiheaded.disbot.settings.Constants;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class DisBot {
    private static final Logger logger = LoggerFactory.getLogger(DisBot.class);

    public static Settings settings;

    private static void setupBot() {
        try {
            settings = SettingsManager.getInstance().getSettings();
            EventWaiter waiter = new EventWaiter();

            CommandClientBuilder commandClientBuilder = new CommandClientBuilder()
                    .setEmojis("\uD83D\uDC4C", "\uD83D\uDD95", "\uD83D\uDCA2")
                    .setPrefix(Constants.BOT_PREFIX)
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setGame(Game.watching("Goblin Slayer"))
                    .setOwnerId(settings.ownerId)
                    .addCommands(
                            new BackupCommand(),
                            new EmojiStatsCommand(waiter),
                            new ShutdownCommand(),
                            new PingCommand()
                    );

            new JDABuilder(AccountType.BOT)
                    .setToken(settings.token)
                    .addEventListener(
                            waiter,
                            commandClientBuilder.build()
                    )
                    .build();

        } catch (LoginException le) {
            logger.error("Invalid username and/or password.");
            System.exit(Constants.BAD_USERNAME_PASS_COMBO);
        }
    }

    public static void main(String[] args) {
        setupBot();
    }
}
