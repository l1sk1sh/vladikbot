package com.multiheaded.disbot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.multiheaded.disbot.audio.NowplayingHandler;
import com.multiheaded.disbot.audio.PlayerManager;
import com.multiheaded.disbot.models.playlist.PlaylistLoader;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Removal of GUI
 * @author John Grosh
 */
public class Bot {
    private final EventWaiter waiter;
    private final ScheduledExecutorService threadpool;
    private final Settings settings = SettingsManager.getInstance().getSettings();
    private final PlayerManager players;
    private final PlaylistLoader playlists;
    private final NowPlayingHandler nowPlaying;

    private boolean shuttingDown = false;
    private JDA jda;

    Bot(EventWaiter waiter) {
        this.threadpool = Executors.newSingleThreadScheduledExecutor();
        this.waiter = waiter;
        this.playlists = new PlaylistLoader();
        this.players = new PlayerManager(this);
        this.players.init();
        this.nowPlaying = new NowplayingHandler(this);
        this.nowPlaying.init();
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
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
            threadpool.submit(() -> guild.getAudioManager().closeAudioConnection());
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
        threadpool.shutdownNow();
        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.getGuilds().stream().forEach(g -> {
            });
            {
                g.getAudioManager().closeAudioConnection();
                AudioHandler ah = (AudioHandler) g.getAudioManager().getSendingHandler();
                if (ah != null) {
                    ah.stopAndClear();
                    ah.getPlayer().destroy();
                    nowplaying.updateTopic(g.getIdLong(), ah, true);
                }
            });

            jda.shutdown();
        }
        System.exit(0);
    }

    public void setJDA(JDA jda) {
        this.jda = jda;
    }
}
