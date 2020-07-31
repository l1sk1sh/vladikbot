package com.l1sk1sh.vladikbot.services.rss;

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import com.l1sk1sh.vladikbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RssFeedTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RssFeedTask.class);

    private final RssResource resource;
    private final String rssUrl;
    private final String resourceImageUrl;
    private final Color newsColor;
    private long lastSentArticle;
    private Bot bot;

    RssFeedTask(RssResource resource, String rssUrl, String resourceImageUrl, Color newsColor, Bot bot) {
        this.resource = resource;
        this.rssUrl = rssUrl;
        this.resourceImageUrl = resourceImageUrl;
        this.newsColor = newsColor;
        this.lastSentArticle = bot.getOfflineStorage().getLastArticleTime(resource);
        this.bot = bot;
    }

    @Override
    public void run() {
        try {
            log.info("Running {} article lookup...", resource);
            RssReader reader = new RssReader();
            Stream<Item> rssFeed = reader.read(rssUrl);
            List<Item> articles = rssFeed.limit(1).collect(Collectors.toList());
            Item mostRecentArticle = articles.get(0);

            Date articleDate = ArticleMapper.getDateFromArticle(mostRecentArticle);
            if (articleDate == null || (lastSentArticle != 0L && articleDate.getTime() == lastSentArticle)) {
                return;
            }

            lastSentArticle = articleDate.getTime();
            bot.getOfflineStorage().setLastArticleTime(resource, lastSentArticle);

            log.debug("Sending {} article ({}).", resource, lastSentArticle);
            bot.getNewsNotificationService().sendNewsArticle(null, ArticleMapper.mapRssToNewsMessage(mostRecentArticle, resource, resourceImageUrl), newsColor);
        } catch (IOException | ParseException e) {
            log.error("Failed to get {} article.", resource, e);
        }
    }
}
