package com.l1sk1sh.vladikbot;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.Objects;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.l1sk1sh.vladikbot.services.*;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.settings.BotSettings;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.GuildSpecificSettings;
import com.l1sk1sh.vladikbot.settings.GuildSpecificSettingsManager;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class Bot {
    private final EventWaiter waiter;
    private final ScheduledExecutorService threadPool;
    private final BotSettingsManager botSettingsManager;
    private final GuildSpecificSettingsManager guildSpecificSettingsManager;
    private final PlayerManager playerManager;
    private final PlaylistLoader playlistLoader;
    private final NowPlayingHandler nowPlayingHandler;
    private final AutoReplyManager autoReplyManager;
    private final GameAndActionSimulationManager gameAndActionSimulationManager;
    private final AutoTextBackupDaemon autoTextBackupDaemon;
    private final AutoMediaBackupDaemon autoMediaBackupDaemon;
    private final ChatNotificationService notificationService;
    private final DockerVerificationService dockerVerificationService;

    private boolean lockedBackup = false;
    private boolean lockedAutoBackup = false;
    private boolean shuttingDown = false;
    private boolean dockerFailed = false;
    private JDA jda;

    public Bot(EventWaiter waiter, BotSettingsManager botSettingsManager, GuildSpecificSettingsManager guildSpecificSettingsManager) {
        this.waiter = waiter;
        this.botSettingsManager = botSettingsManager;
        this.guildSpecificSettingsManager = guildSpecificSettingsManager;
        this.playlistLoader = new PlaylistLoader(this);
        this.threadPool = Executors.newSingleThreadScheduledExecutor();
        this.playerManager = new PlayerManager(this);
        this.playerManager.init();
        this.nowPlayingHandler = new NowPlayingHandler(this);
        this.nowPlayingHandler.init();
        this.autoReplyManager = new AutoReplyManager(this);
        this.gameAndActionSimulationManager = new GameAndActionSimulationManager(this);
        this.autoTextBackupDaemon = new AutoTextBackupDaemon(this);
        this.autoMediaBackupDaemon = new AutoMediaBackupDaemon(this);
        this.notificationService = new ChatNotificationService(this);
        this.dockerVerificationService = new DockerVerificationService();
    }

    public void closeAudioConnection(long guildId) {
        Guild guild = jda.getGuildById(guildId);
        if (guild != null) {
            threadPool.submit(() -> guild.getAudioManager().closeAudioConnection());
        }
    }

    public void resetGame() {
        Game game = botSettingsManager.getSettings().getGame() == null
                || botSettingsManager.getSettings().getGame().getName().equalsIgnoreCase("none")
                ? null : botSettingsManager.getSettings().getGame();
        if (!Objects.equals(jda.getPresence().getGame(), game)) {
            jda.getPresence().setGame(game);
        }
    }

    public void shutdown() {
        // TODO Why using System.exit Try shutting down all threads
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
                    nowPlayingHandler.updateTopic(g.getIdLong(), audioHandler, true);
                }
            });

            jda.shutdown();
        }
        SystemUtils.exit(0, 5000);
    }

    public BotSettings getBotSettings() {
        return botSettingsManager.getSettings();
    }

    public GuildSpecificSettings getGuildSettings(Guild guild) {
        return guildSpecificSettingsManager.getSettings(guild);
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public ScheduledExecutorService getThreadPool() {
        return threadPool;
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

    public AutoTextBackupDaemon getAutoTextBackupDaemon() {
        return autoTextBackupDaemon;
    }

    public AutoMediaBackupDaemon getAutoMediaBackupDaemon() {
        return autoMediaBackupDaemon;
    }

    public ChatNotificationService getNotificationService() {
        return notificationService;
    }

    public DockerVerificationService getDockerVerificationService() {
        return dockerVerificationService;
    }

    public boolean isDockerFailed() {
        return dockerFailed;
    }

    public void setDockerFailed(boolean dockerFailed) {
        this.dockerFailed = dockerFailed;
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
}
