package com.l1sk1sh.vladikbot;

import com.github.ygimenez.exception.InvalidHandlerException;
import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Paginator;
import com.github.ygimenez.model.PaginatorBuilder;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.l1sk1sh.vladikbot.commands.admin.*;
import com.l1sk1sh.vladikbot.commands.dj.*;
import com.l1sk1sh.vladikbot.commands.everyone.*;
import com.l1sk1sh.vladikbot.commands.music.*;
import com.l1sk1sh.vladikbot.commands.owner.*;
import com.l1sk1sh.vladikbot.contexts.DickContextMenu;
import com.l1sk1sh.vladikbot.models.queue.QueueType;
import com.l1sk1sh.vladikbot.services.audio.AloneInVoiceHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.services.logging.GuildLoggerService;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 */
@Slf4j
@SpringBootApplication
public class VladikBot {

    private static JDA jda;
    private final BotSettingsManager settings;

    @Setter(onMethod = @__({@Autowired}))
    private Listener listener;
    @Setter(onMethod = @__({@Autowired}))
    private EventWaiter eventWaiter;
    @Setter(onMethod = @__({@Autowired}))
    private PlayerManager playerManager;
    @Setter(onMethod = @__({@Autowired}))
    private NowPlayingHandler nowPlayingHandler;
    @Setter(onMethod = @__({@Autowired}))
    private AloneInVoiceHandler aloneInVoiceHandler;
    @Setter(onMethod = @__({@Autowired}))
    private AutoReplyManager autoReplyManager;
    @Setter(onMethod = @__({@Autowired}))
    private GuildLoggerService guildLoggerService;

    @Autowired
    public VladikBot(BotSettingsManager settings) {
        this.settings = settings;
    }

    public static void main(String[] args) {
        if (!String.join("", args).contains("db.password")) {
            System.err.println("'db.password' property must be specified as program arguments.");
            SystemUtils.exit(1);
        }

        SpringApplication.run(VladikBot.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        Charset defaultCharset = Charset.defaultCharset();
        String utf8canonical = "utf-8";

        if (!defaultCharset.toString().equalsIgnoreCase(utf8canonical)) {
            log.warn("Default charset is '{}'. Consider changing to 'UTF-8' by setting JVM options '-Dconsole.encoding=UTF-8 -Dfile.encoding=UTF-8'.", defaultCharset);
        }

        if (!System.getProperty("java.vm.name").contains("64")) {
            log.warn("It appears that you may not be using a supported Java version. Please use 64-bit java.");
        }

        try {
            settings.init();
        } catch (IOException e) {
            log.error("Error while reading settings.", e);
            SystemUtils.exit(1);
        }

        playerManager.init();
        nowPlayingHandler.init();
        aloneInVoiceHandler.init();
        autoReplyManager.init();
        guildLoggerService.init();
        minecraftServerCommand.init();

        CommandClientBuilder commandClientBuilder = new CommandClientBuilder()
                .setPrefix(settings.get().getPrefix())
                .setOwnerId(Long.toString(settings.get().getOwnerId()))
                .setEmojis(settings.get().getSuccessEmoji(), settings.get().getWarningEmoji(), settings.get().getErrorEmoji())
                .setLinkedCacheSize(1024)
                // Use this forcing during development only
                .forceGuildOnly((settings.get().getForceGuildId() > 0L)
                        ? String.valueOf(settings.get().getForceGuildId())
                        : null)
                .addContextMenus(
                        dickContextMenu
                )
                .addSlashCommands(
                        /* Everyone commands */
                        catFactCommand,
                        catGirlPictureCommand,
                        waifuPictureCommand,
                        catPictureCommand,
                        countryCommand,
                        dickCommand,
                        dogFactCommand,
                        dogPictureCommand,
                        flipCoinCommand,
                        jokeCommand,
                        pingCommand,
                        quoteCommand,
                        rollDiceCommand,
                        songInfoCommand,
                        settingsCommand,
                        statusCommand,

                        /* Music commands */
                        nowPlayingCommand,
                        playCommand,
                        playPlaylistCommand,
                        queueCommand,
                        removeCommand,
                        searchCommand,
                        shuffleCommand,
                        skipCommand,
                        soundCloudSearchCommand,

                        /* Music commands DJ only */
                        moveTrackCommand,
                        pauseCommand,
                        playNextCommand,
                        repeatCommand,
                        skipForceCommand,
                        skipToCommand,
                        stopCommand,
                        volumeCommand,

                        /* Administrator commands */
                        activitySimulationCommand,
                        autoReplyCommand,
                        backupMediaCommand,
                        backupTextCommand,
                        emojiStatsCommand,
                        guildLoggerCommand,
                        ipCommand,
                        memesManagementCommand,
                        minecraftServerCommand,
                        newsManagementCommand,
                        permissionsCommand,
                        privateMessageCommand,
                        queueTypeCommand,
                        playlistCommand,
                        reminderCommand,
                        setDjCommand,
                        setNotificationChannelCommand,
                        setSkipAudioRatioCommand,
                        setTextChannelCommand,
                        setVoiceChannelCommand,

                        /* Owner commands */
                        setAvatarCommand,
                        setIPCommand,
                        setPresenceCommand,
                        setStatusCommand,
                        shutdownCommand
                );

        try {
            jda = JDABuilder.create(settings.get().getToken(), Const.REQUIRED_INTENTS)
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .disableCache(CacheFlag.SCHEDULED_EVENTS, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.ROLE_TAGS)
                    .setBulkDeleteSplittingEnabled(true)
                    .addEventListeners(
                            eventWaiter,
                            commandClientBuilder.build(),
                            listener
                    )
                    .build();
            // This might be required if bot is in more than 100 guilds
            Message.suppressContentIntentWarning();
            PaginatorBuilder.createPaginator(jda)
                    .shouldEventLock(true)
                    .setDeleteOnCancel(true)
                    .shouldRemoveOnReact(false)
                    .activate();
        } catch (InvalidTokenException e) {
            log.error("Invalid username and/or password.");
            SystemUtils.exit(1);
        } catch (InvalidHandlerException e) {
            log.error("Pagination could not be started.");
            SystemUtils.exit(1);
        } catch (ErrorResponseException e) {
            log.error("Invalid response returned when attempting to connect. " +
                    "Please make sure you're connected to the internet");
            System.exit(1);
        }
    }

    /**
     * Should be called only after initialization, that is done later than beans initialization.
     */
    public static JDA jda() {
        return jda;
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
        return Executors.newScheduledThreadPool(5);
    }

    @Bean(name = "backupThreadPool")
    @Scope("singleton")
    public ScheduledExecutorService backupThreadPool() {
        return Executors.newScheduledThreadPool(2);
    }

    /**
     * Commands used in the Bot
     */
    @Setter(onMethod = @__({@Autowired}))
    private DickContextMenu dickContextMenu;
    @Setter(onMethod = @__({@Autowired}))
    private ActivitySimulationCommand activitySimulationCommand;
    @Setter(onMethod = @__({@Autowired}))
    private AutoReplyCommand autoReplyCommand;
    @Setter(onMethod = @__({@Autowired}))
    private BackupTextCommand backupTextCommand;
    @Setter(onMethod = @__({@Autowired}))
    private BackupMediaCommand backupMediaCommand;
    @Setter(onMethod = @__({@Autowired}))
    private EmojiStatsCommand emojiStatsCommand;
    @Setter(onMethod = @__({@Autowired}))
    private GuildLoggerCommand guildLoggerCommand;
    @Setter(onMethod = @__({@Autowired}))
    private IPCommand ipCommand;
    @Setter(onMethod = @__({@Autowired}))
    private MemesManagementCommand memesManagementCommand;
    @Setter(onMethod = @__({@Autowired}))
    private NewsManagementCommand newsManagementCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PermissionsCommand permissionsCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PrivateMessageCommand privateMessageCommand;
    @Setter(onMethod = @__({@Autowired}))
    private QueueTypeCommand queueTypeCommand;
    @Setter(onMethod = @__({@Autowired}))
    private MinecraftServerCommand minecraftServerCommand;
    @Setter(onMethod = @__({@Autowired}))
    private ReminderCommand reminderCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetDjCommand setDjCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetNotificationChannelCommand setNotificationChannelCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetSkipAudioRatioCommand setSkipAudioRatioCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetTextChannelCommand setTextChannelCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetVoiceChannelCommand setVoiceChannelCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SkipForceCommand skipForceCommand;
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
    private CatGirlPictureCommand catGirlPictureCommand;
    @Setter(onMethod = @__({@Autowired}))
    private WaifuPictureCommand waifuPictureCommand;
    @Setter(onMethod = @__({@Autowired}))
    private CountryCommand countryCommand;
    @Setter(onMethod = @__({@Autowired}))
    private DickCommand dickCommand;
    @Setter(onMethod = @__({@Autowired}))
    private DogFactCommand dogFactCommand;
    @Setter(onMethod = @__({@Autowired}))
    private DogPictureCommand dogPictureCommand;
    @Setter(onMethod = @__({@Autowired}))
    private FlipCoinCommand flipCoinCommand;
    @Setter(onMethod = @__({@Autowired}))
    private JokeCommand jokeCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PingCommand pingCommand;
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
    private NowPlayingCommand nowPlayingCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PlayCommand playCommand;
    @Setter(onMethod = @__({@Autowired}))
    private PlayPlaylistCommand playPlaylistCommand;
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
    private PlaylistCommand playlistCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetAvatarCommand setAvatarCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetIPCommand setIPCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetPresenceCommand setPresenceCommand;
    @Setter(onMethod = @__({@Autowired}))
    private SetStatusCommand setStatusCommand;
    @Setter(onMethod = @__({@Autowired}))
    private ShutdownCommand shutdownCommand;
}
