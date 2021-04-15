package com.l1sk1sh.vladikbot.services.rss;

import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;
import com.l1sk1sh.vladikbot.data.entity.SentNewsArticle;
import com.l1sk1sh.vladikbot.data.repository.SentNewsArticleRepository;
import com.l1sk1sh.vladikbot.services.notification.NewsNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author l1sk1sh
 */
final class RssFeedTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RssFeedTask.class);

    private static final int ARTICLE_FETCH_LIMIT = 8;

    private final SentNewsArticleRepository sentNewsArticleRepository;
    private final NewsNotificationService newsNotificationService;
    private final RssService.RssResource resource;
    private final String rssUrl;
    private final String resourceImageUrl;
    private final Color newsColor;

    RssFeedTask(SentNewsArticleRepository sentNewsArticleRepository, NewsNotificationService newsNotificationService,
                RssService.RssResource resource, String rssUrl, String resourceImageUrl, Color newsColor) {
        this.sentNewsArticleRepository = sentNewsArticleRepository;
        this.newsNotificationService = newsNotificationService;
        this.resource = resource;
        this.rssUrl = rssUrl;
        this.resourceImageUrl = resourceImageUrl;
        this.newsColor = newsColor;
    }

    @Override
    public void run() {
        try {
            log.debug("Running {} article lookup...", resource);
            RssReader reader = new RssReader();
            Stream<Item> rssFeed = reader.read(rssUrl);
            List<Item> articles = rssFeed.limit(ARTICLE_FETCH_LIMIT).collect(Collectors.toList());
            Item lastAddedArticle = null;

            List<SentNewsArticle> lastSentArticles = sentNewsArticleRepository.findTop20ByNewsResourceOrderByIdDesc(resource);
            List<SentNewsArticle> newSentArticles = new ArrayList<>();

            // Iterate in revers to get older articles first
            for (int i = articles.size() - 1; i >= 0; i--) {
                String articleId = "";
                if (articles.get(i).getGuid().isPresent()) {
                    articleId = articles.get(i).getGuid().get();
                }
                if (articleId.isEmpty()) {
                    articleId = ArticleMapper.getTitleAsId(articles.get(i));
                }

                String finalArticleId = articleId;
                boolean articleAlreadySent = lastSentArticles.stream().anyMatch(sentArticle -> sentArticle.getArticleId().equals(finalArticleId));
                if (!articleAlreadySent) {
                    newSentArticles.add(new SentNewsArticle(resource, articleId));
                    lastAddedArticle = articles.get(i);
                    break;
                }
            }

            if (lastAddedArticle == null) {
                return;
            }

            sentNewsArticleRepository.saveAll(newSentArticles);

            log.info("Sending {} article ({}).", resource, lastAddedArticle.getLink());
            newsNotificationService.sendNewsArticle(null, ArticleMapper.mapRssArticleToNewsMessage(lastAddedArticle, resource, resourceImageUrl), newsColor);
        } catch (ConnectException | HttpTimeoutException | UnresolvedAddressException e) {
            log.warn("Failed to get {} article due to network issues.", resource);
        } catch (IOException e) {
            log.error("Failed to get {} article due to unknown reason.", resource, e);
        }
    }
}
