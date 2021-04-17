package com.l1sk1sh.vladikbot.services.rss;

import com.apptastic.rssreader.Item;
import com.l1sk1sh.vladikbot.models.NewsDiscordMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author l1sk1sh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ArticleMapper {
    private static final Logger log = LoggerFactory.getLogger(ArticleMapper.class);

    private static final String EMPTY_TITLE = "Empty title";
    private static final String DESCRIPTION_CUT_ENDING = "[…]";
    private static final int DESCRIPTION_MAX_LENGTH = 350;

    static NewsDiscordMessage mapRssArticleToNewsMessage(Item article, RssService.RssResource resource, String resourceImageUrl) {
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
            description = getNormalizedDescription(description);
        }

        if (article.getLink().isPresent()) {
            articleUrl = article.getLink().get();
        }

        try {
            publicationDate = getDateFromArticle(article);
        } catch (ParseException e) {
            log.warn("Failed to parse {} publication date.", resource, e);
        }

        return new NewsDiscordMessage(
                article.getTitle().isPresent() ? article.getTitle().get() : "",
                description,
                imageUrl,
                articleUrl,
                publicationDate,
                resourceImageUrl
        );
    }

    private static Date getDateFromArticle(Item article) throws ParseException {
        if (article.getPubDate().isEmpty()) {
            log.warn("Article doesn't have date.");
            return null;
        }

        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").parse(article.getPubDate().get());
    }

    private static String getNormalizedDescription(String description) {
        String desc = description.replaceAll(DESCRIPTION_CUT_ENDING, "");
        if (desc.length() > DESCRIPTION_MAX_LENGTH) {
            int indexOfNextSpace = desc.indexOf(' ', DESCRIPTION_MAX_LENGTH);
            desc = desc.substring(0, indexOfNextSpace);
        }
        return desc + " " + DESCRIPTION_CUT_ENDING;
    }

    static String getTitleAsId(Item article) {
        if (article.getTitle().isEmpty()) {
            return EMPTY_TITLE;
        }

        return article.getTitle().get().trim().toLowerCase().replaceAll(" ", "_").replaceAll("[^a-zа-я_0-9]", "");
    }
}
