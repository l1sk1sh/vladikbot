package l1.multiheaded.vladikbot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import l1.multiheaded.vladikbot.commands.everyone.StatusCommand;
import l1.multiheaded.vladikbot.commands.admin.*;
import l1.multiheaded.vladikbot.commands.dj.*;
import l1.multiheaded.vladikbot.commands.everyone.SettingsCommand;
import l1.multiheaded.vladikbot.commands.music.*;
import l1.multiheaded.vladikbot.commands.owner.*;
import l1.multiheaded.vladikbot.settings.BotSettings;
import l1.multiheaded.vladikbot.settings.BotSettingsManager;
import l1.multiheaded.vladikbot.settings.GuildSettingsManager;
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
class VladikBot {
    private static final Logger log = LoggerFactory.getLogger(VladikBot.class);

    public static void main(String[] args) {
        try {
            EventWaiter waiter = new EventWaiter();
            BotSettingsManager botSettingsManager = new BotSettingsManager();
            GuildSettingsManager guildSettingsManager = new GuildSettingsManager();
            BotSettings botSettings = botSettingsManager.getSettings();

            Bot bot = new Bot(waiter, botSettingsManager, guildSettingsManager);

            CommandClientBuilder commandClientBuilder = new CommandClientBuilder()
                    .setPrefix(botSettings.getPrefix())
                    .setOwnerId(Long.toString(botSettings.getOwnerId()))
                    .setEmojis(botSettings.getSuccessEmoji(), botSettings.getWarningEmoji(), botSettings.getErrorEmoji())
                    .setHelpWord(botSettings.getHelpWord())
                    .setStatus((botSettings.getOnlineStatus() != OnlineStatus.UNKNOWN)
                            ? botSettings.getOnlineStatus() : OnlineStatus.DO_NOT_DISTURB)
                    .setGame((botSettings.getGame() != null)
                            ? botSettings.getGame() : Game.playing("your dad"))
                    .setLinkedCacheSize(200)
                    .addCommands(
                            new PingCommand(),
                            new SettingsCommand(botSettings, guildSettingsManager),
                            new StatusCommand(botSettings),
                            new DebugCommand(bot),

                            new SetNotificationChannelCommand(bot),
                            new SetDjCommand(bot),
                            new SetTextChannelCommand(bot),
                            new SetVoiceChannelCommand(bot),

                            new PermissionsCommand(),
                            new BackupMediaCommand(bot),
                            new BackupChannelCommand(bot),
                            new EmojiStatsCommand(waiter, bot),
                            new AutoModerationCommand(bot),
                            new RotatingActionAndGameCommand(bot),

                            new ForceSkipCommand(bot),
                            new PauseCommand(bot),
                            new PlayNextCommand(bot),
                            new RepeatCommand(bot),
                            new SkipToCommand(bot),
                            new StopCommand(bot),
                            new VolumeCommand(bot),
                            new MoveTrackCommand(bot),

                            new AutoPlaylistCommand(bot),
                            new PlaylistCommand(bot),
                            new SetAvatarCommand(),
                            new SetGameCommand(),
                            new SetNameCommand(),
                            new SetStatusCommand(),
                            new ClearTmpCommand(bot),
                            new RotatingBackupCommand(bot),

                            new LyricsCommand(bot),
                            new NowPlayingCommand(bot),
                            new PlayCommand(bot),
                            new PlaylistsCommand(bot),
                            new QueueCommand(bot),
                            new RemoveCommand(bot),
                            new SearchCommand(bot),
                            new ShuffleCommand(bot),
                            new SkipCommand(bot),
                            new SoundCloudSearchCommand(bot),

                            new ShutdownCommand(bot)
                    );

            JDA jda = new JDABuilder(AccountType.BOT)
                    .setToken(botSettings.getToken())
                    .setAudioEnabled(true)
                    .addEventListener(
                            waiter,
                            commandClientBuilder.build(),
                            new Listener(bot)
                    )
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
            bot.setJDA(jda);
        } catch (ExceptionInInitializerError e) {
            log.error("Problematic botSettings input.");
            System.exit(1);
        } catch (LoginException le) {
            log.error("Invalid username and/or password.");
            System.exit(1);
        }
    }
}
