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
    private final AutoModerationManager autoModerationManager;
    private final ActionAndGameRotationManager actionAndGameRotationManager;
    private final RotatingBackupChannelService rotatingBackupChannelService;
    private final RotatingBackupMediaService rotatingBackupMediaService;
    private final ChatNotificationService notificationService;

    private boolean lockedBackup = false;
    private boolean lockedRotationBackup = false;
    private boolean shuttingDown = false;
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
        this.autoModerationManager = new AutoModerationManager(this);
        this.actionAndGameRotationManager = new ActionAndGameRotationManager(this);
        this.rotatingBackupChannelService = new RotatingBackupChannelService(this);
        this.rotatingBackupMediaService = new RotatingBackupMediaService(this);
        this.notificationService = new ChatNotificationService(this);
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
        return (GuildSpecificSettings) guildSpecificSettingsManager.getSettings(guild);
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

    public boolean isLockedRotationBackup() {
        return lockedRotationBackup;
    }

    public void setLockedRotationBackup(boolean lockedRotationBackup) {
        this.lockedRotationBackup = lockedRotationBackup;
    }

    public AutoModerationManager getAutoModerationManager() {
        return autoModerationManager;
    }

    public ActionAndGameRotationManager getActionAndGameRotationManager() {
        return actionAndGameRotationManager;
    }

    public RotatingBackupChannelService getRotatingBackupChannelService() {
        return rotatingBackupChannelService;
    }

    public RotatingBackupMediaService getRotatingBackupMediaService() {
        return rotatingBackupMediaService;
    }

    public ChatNotificationService getNotificationService() {
        return notificationService;
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
