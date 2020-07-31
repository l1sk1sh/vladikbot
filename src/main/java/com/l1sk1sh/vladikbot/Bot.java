package com.l1sk1sh.vladikbot;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.google.gson.Gson;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.l1sk1sh.vladikbot.services.ReminderService;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.services.audio.PlaylistLoader;
import com.l1sk1sh.vladikbot.services.backup.AutoMediaBackupDaemon;
import com.l1sk1sh.vladikbot.services.backup.AutoTextBackupDaemon;
import com.l1sk1sh.vladikbot.services.backup.DockerService;
import com.l1sk1sh.vladikbot.services.logging.GuildLoggerService;
import com.l1sk1sh.vladikbot.services.logging.MessageCache;
import com.l1sk1sh.vladikbot.services.notification.ChatNotificationService;
import com.l1sk1sh.vladikbot.services.notification.NewsNotificationService;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.services.presence.GameAndActionSimulationManager;
import com.l1sk1sh.vladikbot.services.retrievers.RandomQuoteRetriever;
import com.l1sk1sh.vladikbot.services.rss.RssService;
import com.l1sk1sh.vladikbot.settings.*;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class Bot {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    private final EventWaiter waiter;
    private final ScheduledExecutorService frontThreadPool;
    private final ScheduledExecutorService backThreadPool;
    private final BotSettingsManager botSettingsManager;
    private final GuildSpecificSettingsManager guildSpecificSettingsManager;
    private final OfflineStorageManager offlineStorageManager;
    private final PlayerManager playerManager;
    private final PlaylistLoader playlistLoader;
    private final NowPlayingHandler nowPlayingHandler;
    private final AutoReplyManager autoReplyManager;
    private final GameAndActionSimulationManager gameAndActionSimulationManager;
    private final GuildLoggerService guildLoggerService;
    private final AutoTextBackupDaemon autoTextBackupDaemon;
    private final AutoMediaBackupDaemon autoMediaBackupDaemon;
    private final ChatNotificationService notificationService;
    private final NewsNotificationService newsNotificationService;
    private final DockerService dockerService;
    private final RandomQuoteRetriever randomQuoteRetriever;
    private final MessageCache messageCache;
    private final ReminderService reminderService;
    private final RssService rssService;

    public static Random rand;
    public static Gson gson;
    public static OkHttpClient httpClient;

    private boolean lockedBackup = false;
    private boolean lockedAutoBackup = false;
    private boolean shuttingDown = false;
    private boolean dockerRunning = false;
    private JDA jda;

    public Bot(EventWaiter waiter, BotSettingsManager botSettingsManager,
               GuildSpecificSettingsManager guildSpecificSettingsManager, OfflineStorageManager offlineStorageManager) {
        this.waiter = waiter;
        this.botSettingsManager = botSettingsManager;
        this.guildSpecificSettingsManager = guildSpecificSettingsManager;
        this.offlineStorageManager = offlineStorageManager;
        this.playlistLoader = new PlaylistLoader(this);
        this.frontThreadPool = Executors.newSingleThreadScheduledExecutor();
        this.backThreadPool = Executors.newSingleThreadScheduledExecutor();
        this.playerManager = new PlayerManager(this);
        this.playerManager.init();
        this.nowPlayingHandler = new NowPlayingHandler(this);
        this.nowPlayingHandler.init();
        this.autoReplyManager = new AutoReplyManager(this);
        this.gameAndActionSimulationManager = new GameAndActionSimulationManager(this);
        this.guildLoggerService = new GuildLoggerService(this);
        this.autoTextBackupDaemon = new AutoTextBackupDaemon(this);
        this.autoMediaBackupDaemon = new AutoMediaBackupDaemon(this);
        this.notificationService = new ChatNotificationService(this);
        this.newsNotificationService = new NewsNotificationService(this);
        this.dockerService = new DockerService(this);
        this.randomQuoteRetriever = new RandomQuoteRetriever();
        this.messageCache = new MessageCache();
        this.reminderService = new ReminderService(this);
        this.rssService = new RssService(this);
    }

    public void closeAudioConnection(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            frontThreadPool.submit(() -> guild.getAudioManager().closeAudioConnection());
        }
    }

    public void resetGame() {
        Activity activity = botSettingsManager.getSettings().getActivity() == null
                || botSettingsManager.getSettings().getActivity().getName().equalsIgnoreCase("none")
                ? null : botSettingsManager.getSettings().getActivity();
        if (!Objects.equals(jda.getPresence().getActivity(), activity)) {
            jda.getPresence().setActivity(activity);
        }
    }

    public void shutdown() {
        if (shuttingDown) {
            return;
        }

        shuttingDown = true;
        frontThreadPool.shutdownNow();
        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.getGuilds().forEach(g -> {
                g.getAudioManager().closeAudioConnection();
                AudioHandler audioHandler = (AudioHandler) g.getAudioManager().getSendingHandler();
                if (audioHandler != null) {
                    audioHandler.stopAndClear();
                    audioHandler.getPlayer().destroy();
                    nowPlayingHandler.updateTopic(g.getIdLong(), audioHandler, true);
                }
            });

            jda.shutdown();
        }

        for (Thread t: Thread.getAllStackTraces().keySet()) {
            log.debug(t.toString());
        }

        /* Unfortunately, JDA doesn't close all it's connection, even though bot technically is shut down and doesn't
        * receive commands. Might be subject for further research */
        SystemUtils.exit(0);
    }

    public BotSettings getBotSettings() {
        return botSettingsManager.getSettings();
    }

    public GuildSpecificSettings getGuildSettings(Guild guild) {
        return guildSpecificSettingsManager.getSettings(guild);
    }

    public OfflineStorage getOfflineStorage() {
        return offlineStorageManager.getSettings();
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public ScheduledExecutorService getFrontThreadPool() {
        return frontThreadPool;
    }

    public ScheduledExecutorService getBackThreadPool() {
        return backThreadPool;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public PlaylistLoader getPlaylistLoader() {
        return playlistLoader;
    }

    public NowPlayingHandler getNowPlayingHandler() {
        return nowPlayingHandler;
    }

    void setJDA(JDA jda) {
        this.jda = jda;
    }

    public JDA getJDA() {
        return jda;
    }

    public boolean isLockedBackup() {
        return lockedBackup;
    }

    public void setLockedBackup(boolean lockedBackup) {
        this.lockedBackup = lockedBackup;
    }

    public boolean isLockedAutoBackup() {
        return lockedAutoBackup;
    }

    public void setLockedAutoBackup(boolean lockedAutoBackup) {
        this.lockedAutoBackup = lockedAutoBackup;
    }

    public AutoReplyManager getAutoReplyManager() {
        return autoReplyManager;
    }

    public GameAndActionSimulationManager getGameAndActionSimulationManager() {
        return gameAndActionSimulationManager;
    }

    public GuildLoggerService getGuildLoggerService() {
        return guildLoggerService;
    }

    public AutoTextBackupDaemon getAutoTextBackupDaemon() {
        return autoTextBackupDaemon;
    }

    public AutoMediaBackupDaemon getAutoMediaBackupDaemon() {
        return autoMediaBackupDaemon;
    }

    public ChatNotificationService getNotificationService() {
        return notificationService;
    }

    public NewsNotificationService getNewsNotificationService() {
        return newsNotificationService;
    }

    public DockerService getDockerService() {
        return dockerService;
    }

    public RandomQuoteRetriever getRandomQuoteRetriever() {
        return randomQuoteRetriever;
    }

    public MessageCache getMessageCache() {
        return messageCache;
    }

    public ReminderService getReminderService() {
        return reminderService;
    }

    public RssService getRssService() {
        return rssService;
    }

    public boolean isDockerRunning() {
        return dockerRunning;
    }

    void setDockerRunning(boolean dockerRunning) {
        this.dockerRunning = dockerRunning;
    }

    private List<TextChannel> getAllTextChannels() {
        return this.getJDA().getGuilds().stream()
                .map(Guild::getTextChannels).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<TextChannel> getAvailableTextChannels() {
        return this.getAllTextChannels().stream().filter(textChannel ->
                textChannel.getMembers().stream().anyMatch(
                        member -> member.getUser().getAsTag().equals(getJDA().getSelfUser().getAsTag())
                )
        ).collect(Collectors.toList());
    }

    public void resetLoggerContext() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextInitializer ci = new ContextInitializer(lc);
        lc.reset();
        try {

            /* Prefer autoConfig() over JoranConfigurator.doConfigure() so there is no need to find the file manually */
            ci.autoConfig();
        } catch (JoranException e) {

            /* StatusPrinter will try to log this */
            e.printStackTrace();
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }
}
