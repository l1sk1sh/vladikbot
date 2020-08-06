package com.l1sk1sh.vladikbot.services.meme;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.settings.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MemeService {
    private static final Logger log = LoggerFactory.getLogger(MemeService.class);

    private final Bot bot;
    private boolean initialized = false;
    private ScheduledFuture<?> scheduledMemeFeed;

    public MemeService(Bot bot) {
        this.bot = bot;
    }

    public void start() {
        if (!bot.getBotSettings().shouldSendMemes()) {
            if (initialized) {
                stop();
            }
            return;
        }

        if (initialized) {
            log.info("Meme Service is already running.");
            return;
        }

        scheduledMemeFeed = bot.getFrontThreadPool().scheduleWithFixedDelay(new MemeFeedTask(bot), 10, Const.MEMES_UPDATE_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS);

        log.info("Meme Service has been launched.");
        initialized = true;
    }

    public void stop() {
        if (!initialized) {
            log.info("Meme Service is already stopped.");
            return;
        }

        scheduledMemeFeed.cancel(false);

        log.info("Meme Service has been stopped.");
        initialized = false;
    }
}
