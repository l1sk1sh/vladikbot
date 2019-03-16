package com.multiheaded.disbot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.multiheaded.disbot.audio.AudioHandler;
import com.multiheaded.disbot.audio.NowPlayingHandler;
import com.multiheaded.disbot.audio.PlayerManager;
import com.multiheaded.disbot.commands.admin.*;
import com.multiheaded.disbot.commands.dj.*;
import com.multiheaded.disbot.commands.everyone.SettingsCommand;
import com.multiheaded.disbot.commands.music.*;
import com.multiheaded.disbot.commands.owner.*;
import com.multiheaded.disbot.models.playlist.PlaylistLoader;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Oliver Johnson
 */
public class VladikBot {
    private static final Logger logger = LoggerFactory.getLogger(VladikBot.class);

    private final EventWaiter waiter;
    private final ScheduledExecutorService threadPool;
    private final Settings settings = SettingsManager.getInstance().getSettings();
    private final PlayerManager players;
    private final PlaylistLoader playlists;
    private final NowPlayingHandler nowPlaying;

    private boolean shuttingDown = false;
    private JDA jda;

    private VladikBot() {
        this.threadPool = Executors.newSingleThreadScheduledExecutor();
        this.waiter = new EventWaiter();
        this.playlists = new PlaylistLoader();
        this.players = new PlayerManager(this);
        this.players.init();
        this.nowPlaying = new NowPlayingHandler(this);
        this.nowPlaying.init();

        try {
            Settings settings = SettingsManager.getInstance().getSettings();

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

                            new ForceskipCommand(this),
                            new PauseCommand(this),
                            new PlayNextCommand(this, settings.getLoadingEmoji()),
                            new RepeatCommand(this),
                            new SkipToCommand(this),
                            new StopCommand(this),
                            new VolumeCommand(this),

                            new AutoPlaylistCommand(this),
                            new PlaylistCommand(this),
                            new SetAvatarCommand(),
                            new SetGameCommand(),
                            new SetNameCommand(),
                            new SetStatusCommand(),

                            new LyricsCommand(this),
                            new NowPlayingCommand(this),
                            new PlayCommand(this, settings.getLoadingEmoji()),
                            new PlaylistsCommand(this),
                            new QueueCommand(this),
                            new RemoveCommand(this),
                            new SearchCommand(this, settings.getLoadingEmoji()),
                            new ShuffleCommand(this),
                            new SkipCommand(this),
                            new SoundCloudSearchCommand(this, settings.getSearchingEmoji()),

                            new ShutdownCommand(this)
                    );

            CommandClient commandClient = commandClientBuilder.build();

            jda = new JDABuilder(AccountType.BOT)
                    .setToken(settings.getToken())
                    .setAudioEnabled(true)
                    .addEventListener(
                            waiter,
                            commandClient,
                            new Listener(this)
                    )
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
        } catch (ExceptionInInitializerError e) {
            logger.error("Problematic settings input");
            System.exit(1);
        } catch (LoginException le) {
            logger.error("Invalid username and/or password.");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        new VladikBot();
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public ScheduledExecutorService getThreadPool() {
        return threadPool;
    }

    public PlayerManager getPlayerManager() {
        return players;
    }

    public PlaylistLoader getPlaylistLoader() {
        return playlists;
    }

    public NowPlayingHandler getNowPlayingHandler() {
        return nowPlaying;
    }

    public JDA getJDA() {
        return jda;
    }

    public void closeAudioConnection(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            threadPool.submit(() -> guild.getAudioManager().closeAudioConnection());
        }
    }

    public void resetGame() {
        Game game = settings.getGame() == null
                || settings.getGame().getName().equalsIgnoreCase("none") ? null : settings.getGame();
        if (!Objects.equals(jda.getPresence().getGame(), game)) {
            jda.getPresence().setGame(game);
        }
    }

    public void shutdown() {
        if (shuttingDown) {
            return;
        }
        shuttingDown = true;
        threadPool.shutdownNow();
        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.getGuilds().forEach(g -> {
                g.getAudioManager().closeAudioConnection();
                AudioHandler audioHandler = (AudioHandler) g.getAudioManager().getSendingHandler();
                if (audioHandler != null) {
                    audioHandler.stopAndClear();
                    audioHandler.getPlayer().destroy();
                    nowPlaying.updateTopic(g.getIdLong(), audioHandler, true);
                }
            });

            jda.shutdown();
        }
        System.exit(0);
    }
}
