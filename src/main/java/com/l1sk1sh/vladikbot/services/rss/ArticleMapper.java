package com.l1sk1sh.vladikbot.services.rss;

import com.apptastic.rssreader.Item;
import com.l1sk1sh.vladikbot.models.entities.NewsMessage;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
class ArticleMapper {
    private static final Logger log = LoggerFactory.getLogger(ArticleMapper.class);

    static NewsMessage mapRssToNewsMessage(Item article, RssResource resource, String resourceImageUrl) {
        String description = "";
        String imageUrl = null;
        String articleUrl = null;
        Date publicationDate = null;

        if (article.getDescription().isPresent()) {
            Matcher matcher = Pattern.compile("https://.+?.(jpg|png)").matcher(article.getDescription().get());
            if (matcher.find()) {
                imageUrl = matcher.group();
            }

            description = Jsoup.parse(article.getDescription().get()).text();
        }

        if (article.getLink().isPresent()) {
            articleUrl = article.getLink().get();
        }

        try {
            publicationDate = getDateFromArticle(article);
        } catch (ParseException e) {
            log.warn("Failed to parse {} publication date.", resource, e);
        }

        return new NewsMessage(
                article.getTitle().isPresent() ? article.getTitle().get() : "",
                description,
                imageUrl,
                article.getCategory().isPresent() ? article.getCategory().get() : "",
                articleUrl,
                publicationDate,
                resourceImageUrl
        );
    }

    static Date getDateFromArticle(Item article) throws ParseException {
        if (!article.getPubDate().isPresent()) {
            log.warn("Article doesn't have date.");
            return null;
        }

        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").parse(article.getPubDate().get());
    }
}
