package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.services.audio.AloneInVoiceHandler;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Oliver Johnson
 */
@Service
public class ShutdownHandler {
    private static final Logger log = LoggerFactory.getLogger(ShutdownHandler.class);

    private final JDA jda;
    private final ScheduledExecutorService frontThreadPool;
    private final ScheduledExecutorService backgroundThreadPool;
    private final ScheduledExecutorService backupThreadPool;
    private final NowPlayingHandler nowPlayingHandler;

    private boolean shuttingDown = false;

    @Autowired
    public ShutdownHandler(NowPlayingHandler nowPlayingHandler, PlayerManager playerManager,
                           AloneInVoiceHandler aloneInVoiceHandler, AutoReplyManager autoReplyManager, JDA jda,
                           @Qualifier("frontThreadPool") ScheduledExecutorService frontThreadPool,
                           @Qualifier("backgroundThreadPool") ScheduledExecutorService backgroundThreadPool,
                           @Qualifier("backupThreadPool") ScheduledExecutorService backupThreadPool) {
        this.jda = jda;
        this.frontThreadPool = frontThreadPool;
        this.backgroundThreadPool = backgroundThreadPool;
        this.backupThreadPool = backupThreadPool;

        playerManager.init();
        this.nowPlayingHandler = nowPlayingHandler;
        this.nowPlayingHandler.init();
        aloneInVoiceHandler.init();
        autoReplyManager.init();
    }

    public void shutdown() {
        if (shuttingDown) {
            return;
        }

        shuttingDown = true;
        frontThreadPool.shutdownNow();
        backgroundThreadPool.shutdownNow();
        backupThreadPool.shutdownNow();
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

        for (Thread t : Thread.getAllStackTraces().keySet()) {
            log.debug(t.toString());
        }

        /* Unfortunately, JDA doesn't close all it's connection, even though bot technically is shut down and doesn't
         * receive commands. Might be subject for further research */
        SystemUtils.exit(0);
    }
}
