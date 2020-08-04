package com.l1sk1sh.vladikbot.services.rss;

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.settings.Const;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class RssFeedTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RssFeedTask.class);

    private static final int AVERAGE_REPOSTS_AMOUNT = 2;

    private final RssResource resource;
    private final String rssUrl;
    private final String resourceImageUrl;
    private final Color newsColor;
    private CircularFifoQueue<String> lastSentArticleIdS;
    private Bot bot;

    RssFeedTask(RssResource resource, String rssUrl, String resourceImageUrl, Color newsColor, Bot bot) {
        this.resource = resource;
        this.rssUrl = rssUrl;
        this.resourceImageUrl = resourceImageUrl;
        this.newsColor = newsColor;
        this.lastSentArticleIdS = bot.getOfflineStorage().getLastArticleIds(resource);
        this.bot = bot;
    }

    @Override
    public void run() {
        try {
            log.debug("Running {} article lookup...", resource);
            RssReader reader = new RssReader();
            Stream<Item> rssFeed = reader.read(rssUrl);
            List<Item> articles = rssFeed.limit(Const.ARTICLE_FETCH_LIMIT - AVERAGE_REPOSTS_AMOUNT).collect(Collectors.toList());
            Item lastAddedArticle = null;

            // Iterate in revers to get older articles first
            for (int i = articles.size() - 1; i >= 0; i--) {
                String articleId = ArticleMapper.getTitleAsId(articles.get(i));
                if (!lastSentArticleIdS.contains(articleId)) {
                    lastSentArticleIdS.add(articleId);
                    lastAddedArticle = articles.get(i);
                    break;
                }
            }

            if (lastAddedArticle == null) {
                return;
            }

            bot.getOfflineStorage().setLastArticleIds(resource, lastSentArticleIdS);

            log.info("Sending {} article ({}).", resource, lastSentArticleIdS.get(lastSentArticleIdS.size() - 1));
            bot.getNewsNotificationService().sendNewsArticle(null, ArticleMapper.mapRssArticleToNewsMessage(lastAddedArticle, resource, resourceImageUrl), newsColor);
        } catch (IOException e) {
            log.error("Failed to get {} article.", resource, e);
        }
    }
}
