package com.l1sk1sh.vladikbot.services.rss;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.settings.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Oliver Johnson
 */
public class RssService {
    private static final Logger log = LoggerFactory.getLogger(RssService.class);

    private final Bot bot;
    private boolean initialized = false;
    private Set<ScheduledFuture<?>> scheduledRssFeeds = new HashSet<>();

    public RssService(Bot bot) {
        this.bot = bot;
    }

    public void start() {
        if (!bot.getBotSettings().isSendNews()) {
            if (initialized) {
                stop();
            }
            return;
        }

        if (initialized) {
            log.info("RSS Service is already running.");
            return;
        }

        scheduledRssFeeds.add(bot.getFrontThreadPool().scheduleWithFixedDelay(new RssFeedTask(
                RssResource.IGN,
                "https://ru.ign.com/feed.xml",
                "https://ru.ign.com/s/ign/social_logo.png",
                new Color(191, 19, 19),
                bot
        ), 30, Const.NEWS_UPDATE_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS));

        scheduledRssFeeds.add(bot.getFrontThreadPool().scheduleWithFixedDelay(new RssFeedTask(
                RssResource.ITC,
                "https://itc.ua/rss",
                "https://i0.wp.com/itc.ua/wp-content/uploads/2015/05/itc-logo-for-fb.png",
                new Color(38, 38, 38),
                bot
        ), 45, Const.NEWS_UPDATE_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS));

        scheduledRssFeeds.add(bot.getFrontThreadPool().scheduleWithFixedDelay(new RssFeedTask(
                RssResource.GIN,
                "https://www.gameinformer.com/news.xml",
                "https://media.glassdoor.com/sqll/738331/game-informer-squarelogo-1472477253053.png",
                new Color(0, 0, 0),
                bot
        ), 60, Const.NEWS_UPDATE_FREQUENCY_IN_SECONDS, TimeUnit.SECONDS));

        log.info("RSS Service has been launched.");
        initialized = true;
    }

    public void stop() {
        if (!initialized) {
            log.info("RSS Service is already stopped.");
            return;
        }

        scheduledRssFeeds.forEach((sf) -> sf.cancel(false));

        log.info("RSS Service has been stopped.");
        initialized = false;
    }
}
