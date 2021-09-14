package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.utils.SystemUtils;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 */
@RequiredArgsConstructor
@Service
public class ShutdownHandler {
    private static final Logger log = LoggerFactory.getLogger(ShutdownHandler.class);

    @Qualifier("frontThreadPool")
    private final ScheduledExecutorService frontThreadPool;
    @Qualifier("backgroundThreadPool")
    private final ScheduledExecutorService backgroundThreadPool;
    @Qualifier("backupThreadPool")
    private final ScheduledExecutorService backupThreadPool;
    private final NowPlayingHandler nowPlayingHandler;

    private boolean shuttingDown = false;

    public void shutdown() {
        if (shuttingDown) {
            return;
        }

        shuttingDown = true;
        frontThreadPool.shutdownNow();
        backgroundThreadPool.shutdownNow();
        backupThreadPool.shutdownNow();
        JDA jda = VladikBot.jda();
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
