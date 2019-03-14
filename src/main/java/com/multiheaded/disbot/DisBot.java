package com.multiheaded.disbot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.multiheaded.disbot.commands.admin.*;
import com.multiheaded.disbot.commands.dj.*;
import com.multiheaded.disbot.commands.everyone.SettingsCommand;
import com.multiheaded.disbot.commands.music.*;
import com.multiheaded.disbot.commands.owner.*;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

/**
 * @author Oliver Johnson
 */
class DisBot {
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

                            new SetDjCommand(),
                            new SetTextChannelCommand(),
                            new SetVoiceChannelCommand(),
                            new BackupCommand(),
                            new EmojiStatsCommand(waiter),

                            new ForceskipCommand(bot),
                            new PauseCommand(bot),
                            new PlayNextCommand(bot, settings.getLoadingEmoji()),
                            new RepeatCommand(bot),
                            new SkipToCommand(bot),
                            new StopCommand(bot),
                            new VolumeCommand(bot),

                            new AutoPlaylistCommand(bot),
                            new PlaylistCommand(bot),
                            new SetAvatarCommand(),
                            new SetGameCommand(),
                            new SetNameCommand(),
                            new SetStatusCommand(),

                            new LyricsCommand(bot),
                            new NowPlayingCommand(bot),
                            new PlayCommand(bot, settings.getLoadingEmoji()),
                            new PlaylistsCommand(bot),
                            new QueueCommand(bot),
                            new RemoveCommand(bot),
                            new SearchCommand(bot, settings.getLoadingEmoji()),
                            new ShuffleCommand(bot),
                            new SkipCommand(bot),
                            new SoundCloudSearchCommand(bot, settings.getSearchingEmoji()),

                            new ShutdownCommand(bot)
                    );

            CommandClient commandClient = commandClientBuilder.build();

            JDA jda = new JDABuilder(AccountType.BOT)
                    .setToken(settings.getToken())
                    .setAudioEnabled(true)
                    .addEventListener(
                            waiter,
                            commandClient,
                            new Listener(bot)
                    )
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
            bot.setJDA(jda);
        } catch (ExceptionInInitializerError e) {
            logger.error("Problematic settings input");
            System.exit(1);
        } catch (LoginException le) {
            logger.error("Invalid username and/or password.");
            System.exit(1);
        }
    }
}
