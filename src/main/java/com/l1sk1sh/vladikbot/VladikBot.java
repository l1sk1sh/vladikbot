package com.l1sk1sh.vladikbot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.l1sk1sh.vladikbot.commands.admin.*;
import com.l1sk1sh.vladikbot.commands.dj.*;
import com.l1sk1sh.vladikbot.commands.everyone.*;
import com.l1sk1sh.vladikbot.commands.music.*;
import com.l1sk1sh.vladikbot.commands.owner.*;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Oliver Johnson
 */
@SpringBootApplication
class VladikBot {
    private static final Logger log = LoggerFactory.getLogger(VladikBot.class);

    @Setter(onMethod = @__({@Autowired}))
    private ActivitySimulationCommand activitySimulationCommand;
    @Setter(onMethod = @__({@Autowired}))
    private AutoReplyCommand autoReplyCommand;
    @Setter(onMethod = @__({@Autowired}))
    private BackupMediaCommand backupMediaCommand;
    @Setter(onMethod = @__({@Autowired}))
    private BackupTextChannelCommand backupTextChannelCommand;
    @Setter(onMethod = @__({@Autowired}))
    private EmojiStatsCommand emojiStatsCommand;
    @Setter(onMethod = @__({@Autowired}))
    private GuildLoggerCommand guildLoggerCommand;
    @Setter(onMethod = @__({@Autowired}))
    private MemesManagementCommand memesManagementCommand;
    @Setter(onMethod = @__({@Autowired}))
    private NewsManagementCommand newsManagementCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PermissionsCommand permissionsCommand;
    @Setter(onMethod = @__({@Autowired}))
    private ReminderCommand reminderCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SayCommand sayCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetDjCommand setDjCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetNotificationChannelCommand setNotificationChannelCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetTextChannelCommand setTextChannelCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetVoiceChannelCommand setVoiceChannelCommand;
    @Setter(onMethod = @__({@Autowired}))
    private ForceSkipCommand forceSkipCommand;
    @Setter(onMethod = @__({@Autowired}))
    private MoveTrackCommand moveTrackCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PauseCommand pauseCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PlayNextCommand playNextCommand;
    @Setter(onMethod = @__({@Autowired}))
    private RepeatCommand repeatCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SkipToCommand skipToCommand;
    @Setter(onMethod = @__({@Autowired}))
    private StopCommand stopCommand;
    @Setter(onMethod = @__({@Autowired}))
    private VolumeCommand volumeCommand;
    @Setter(onMethod = @__({@Autowired}))
    private CatFactCommand catFactCommand;
    @Setter(onMethod = @__({@Autowired}))
    private CatPictureCommand catPictureCommand;
    @Setter(onMethod = @__({@Autowired}))
    private CountryCommand countryCommand;
    @Setter(onMethod = @__({@Autowired}))
    private DogFactCommand dogFactCommand;
    @Setter(onMethod = @__({@Autowired}))
    private DogPictureCommand dogPictureCommand;
    @Setter(onMethod = @__({@Autowired}))
    private FlipCoinCommand flipCoinCommand;
    @Setter(onMethod = @__({@Autowired}))
    private ISSInfoCommand issInfoCommand;
    @Setter(onMethod = @__({@Autowired}))
    private JokeCommand jokeCommand;
    @Setter(onMethod = @__({@Autowired}))
    private QuoteCommand quoteCommand;
    @Setter(onMethod = @__({@Autowired}))
    private RollDiceCommand rollDiceCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SettingsCommand settingsCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SongInfoCommand songInfoCommand;
    @Setter(onMethod = @__({@Autowired}))
    private StatusCommand statusCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SteamStatusCommand steamStatusCommand;
    @Setter(onMethod = @__({@Autowired}))
    private LyricsCommand lyricsCommand;
    @Setter(onMethod = @__({@Autowired}))
    private NowPlayingCommand nowPlayingCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PlayCommand playCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PlaylistsCommand playlistsCommand;
    @Setter(onMethod = @__({@Autowired}))
    private QueueCommand queueCommand;
    @Setter(onMethod = @__({@Autowired}))
    private RemoveCommand removeCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SearchCommand searchCommand;
    @Setter(onMethod = @__({@Autowired}))
    private ShuffleCommand shuffleCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SkipCommand skipCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SoundCloudSearchCommand soundCloudSearchCommand;
    @Setter(onMethod = @__({@Autowired}))
    private AutoBackupCommand autoBackupCommand;
    @Setter(onMethod = @__({@Autowired}))
    private AutoPlaylistCommand autoPlaylistCommand;
    @Setter(onMethod = @__({@Autowired}))
    private ClearTmpCommand clearTmpCommand;
    @Setter(onMethod = @__({@Autowired}))
    private DebugCommand debugCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PlaylistCommand playlistCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetAvatarCommand setAvatarCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetGameCommand setGameCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetNameCommand setNameCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetStatusCommand setStatusCommand;
    @Setter(onMethod = @__({@Autowired}))
    private ShutdownCommand shutdownCommand;

    @Setter(onMethod = @__({@Autowired}))
    private BotSettingsManager settings;
    @Setter(onMethod = @__({@Autowired}))
    private Listener listener;
    @Setter(onMethod = @__({@Autowired}))
    private EventWaiter eventWaiter;

    private JDA jda;
    private ReadinessListener readinessListener;

    public static void main(String[] args) {
        SpringApplication.run(VladikBot.class, args);
    }

    @Bean
    @Scope("singleton")
    public JDA jda() {
        try {
            String token = BotSettingsManager.readRawToken();

            if (token == null) {
                log.error("Token is missing or empty. Check settings file.");
                SystemUtils.exit(1);
            }

            jda = JDABuilder.create(token, Const.REQUIRED_INTENTS)
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE)
                    .setBulkDeleteSplittingEnabled(true)
                    .build();

            readinessListener = new ReadinessListener();
            jda.addEventListener(readinessListener);

            return jda;
        } catch (LoginException e) {
            log.error("Invalid username and/or password.");
            SystemUtils.exit(1);
        } catch (IOException e) {
            log.error("Error while reading token.", e);
            SystemUtils.exit(1);
        }

        return null;
    }

    @Bean
    @Scope("singleton")
    public EventWaiter eventWaiter() {
        return new EventWaiter();
    }

    @Bean(name = "frontThreadPool")
    @Scope("singleton")
    public ScheduledExecutorService frontThreadPool() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Bean(name = "backgroundThreadPool")
    @Scope("singleton")
    public ScheduledExecutorService backgroundThreadPool() {
        return Executors.newScheduledThreadPool(2);
    }

    @Bean(name = "backupThreadPool")
    @Scope("singleton")
    public ScheduledExecutorService backupThreadPool() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        Charset defaultCharset = Charset.defaultCharset();
        String utf9canonical = "utf-8";

        if (!defaultCharset.toString().equalsIgnoreCase(utf9canonical)) {
            log.warn("Default charset is '{}'. Consider changing to 'UTF-8' by setting JVM options '-Dconsole.encoding=UTF-8 -Dfile.encoding=UTF-8'.", defaultCharset);
        }

        if (!System.getProperty("java.vm.name").contains("64")) {
            log.warn("It appears that you may not be using a supported Java version. Please use 64-bit java.");
        }

        try {
            settings.readSettings();

            CommandClientBuilder commandClientBuilder = new CommandClientBuilder()
                    .setPrefix(settings.get().getPrefix())
                    .setOwnerId(Long.toString(settings.get().getOwnerId()))
                    .setEmojis(settings.get().getSuccessEmoji(), settings.get().getWarningEmoji(), settings.get().getErrorEmoji())
                    .setHelpWord(settings.get().getHelpWord())
                    .setLinkedCacheSize(1024)
                    .addCommands(
                            new PingCommand(),
                            settingsCommand,
                            statusCommand,
                            quoteCommand,
                            songInfoCommand,
                            dogFactCommand,
                            catFactCommand,
                            dogPictureCommand,
                            catPictureCommand,
                            rollDiceCommand,
                            countryCommand,
                            steamStatusCommand,
                            flipCoinCommand,
                            issInfoCommand,
                            jokeCommand,

                            forceSkipCommand,
                            pauseCommand,
                            playNextCommand,
                            repeatCommand,
                            skipToCommand,
                            stopCommand,
                            volumeCommand,
                            moveTrackCommand,
                            lyricsCommand,
                            nowPlayingCommand,
                            playCommand,
                            playlistsCommand,
                            queueCommand,
                            removeCommand,
                            searchCommand,
                            shuffleCommand,
                            skipCommand,
                            soundCloudSearchCommand,

                            reminderCommand,
                            sayCommand,
                            permissionsCommand,
                            backupMediaCommand,
                            backupTextChannelCommand,
                            emojiStatsCommand,
                            autoReplyCommand,
                            activitySimulationCommand,
                            newsManagementCommand,
                            memesManagementCommand,
                            guildLoggerCommand,
                            setNotificationChannelCommand,
                            setDjCommand,
                            setTextChannelCommand,
                            setVoiceChannelCommand,

                            autoPlaylistCommand,
                            playlistCommand,
                            clearTmpCommand,
                            autoBackupCommand,
                            debugCommand,
                            setAvatarCommand,
                            setGameCommand,
                            setNameCommand,
                            setStatusCommand,
                            shutdownCommand
                    );

            jda.addEventListener(
                    eventWaiter,
                    commandClientBuilder.build(),
                    listener
            );
            listener.onReady(readinessListener.getEvent());
        } catch (IOException e) {
            log.error("Error while reading or writing a file.", e);
            SystemUtils.exit(1);
        }
    }

    public static class ReadinessListener extends ListenerAdapter {

        @Getter
        private ReadyEvent event;

        @Override
        public void onReady(@NotNull ReadyEvent event) {
            this.event = event;
        }
    }
}
