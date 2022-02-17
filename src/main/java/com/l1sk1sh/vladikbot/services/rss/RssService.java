package com.l1sk1sh.vladikbot.services.rss;

import com.l1sk1sh.vladikbot.data.repository.SentNewsArticleRepository;
import com.l1sk1sh.vladikbot.services.notification.NewsNotificationService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author l1sk1sh
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RssService {

    @Qualifier("frontThreadPool")
    private final ScheduledExecutorService frontThreadPool;
    private final SentNewsArticleRepository sentNewsArticleRepository;
    private final NewsNotificationService newsNotificationService;
    private final BotSettingsManager settings;
    private final Set<ScheduledFuture<?>> scheduledRssFeeds = new HashSet<>();
    private boolean initialized = false;

    public void start() {
        if (!settings.get().isSendNews()) {
            if (initialized) {
                stop();
            }
            return;
        }

        if (initialized) {
            log.info("RSS Service is already running.");
            return;
        }

        scheduledRssFeeds.add(frontThreadPool.scheduleWithFixedDelay(
                new RssFeedTask(
                        sentNewsArticleRepository,
                        newsNotificationService,
                        RssResource.IGN,
                        "https://ru.ign.com/feed.xml",
                        "https://ru.ign.com/s/ign/social_logo.png",
                        new Color(191, 19, 19)
                ),
                30,
                Const.NEWS_UPDATE_FREQUENCY_IN_SECONDS,
                TimeUnit.SECONDS
        ));

        scheduledRssFeeds.add(frontThreadPool.scheduleWithFixedDelay(
                new RssFeedTask(
                        sentNewsArticleRepository,
                        newsNotificationService,
                        RssResource.ITC,
                        "https://itc.ua/rss",
                        "https://i0.wp.com/itc.ua/wp-content/uploads/2015/05/itc-logo-for-fb.png",
                        new Color(38, 38, 38)
                ),
                45,
                Const.NEWS_UPDATE_FREQUENCY_IN_SECONDS,
                TimeUnit.SECONDS
        ));

        scheduledRssFeeds.add(frontThreadPool.scheduleWithFixedDelay(
                new RssFeedTask(
                        sentNewsArticleRepository,
                        newsNotificationService,
                        RssResource.GIN,
                        "https://www.gameinformer.com/news.xml",
                        "https://media.glassdoor.com/sqll/738331/game-informer-squarelogo-1472477253053.png",
                        new Color(0, 0, 0)
                ),
                60,
                Const.NEWS_UPDATE_FREQUENCY_IN_SECONDS,
                TimeUnit.SECONDS
        ));

        scheduledRssFeeds.add(frontThreadPool.scheduleWithFixedDelay(
                new RssFeedTask(
                        sentNewsArticleRepository,
                        newsNotificationService,
                        RssResource.DTF,
                        "https://dtf.ru/rss/all",
                        "https://dtfstaticbf19cf1-a.akamaihd.net/static/build/dtf.ru/favicons/apple-touch-icon-180x180.png",
                        new Color(217, 245, 255)
                ),
                75,
                Const.NEWS_UPDATE_FREQUENCY_IN_SECONDS,
                TimeUnit.SECONDS
        ));

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

    public enum RssResource {
        IGN,
        ITC,
        GIN,
        DTF
    }
}
