package com.l1sk1sh.vladikbot.data.entity;

import com.l1sk1sh.vladikbot.services.rss.RssService;
import lombok.*;

import javax.persistence.*;

/**
 * @author Oliver Johnson
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "sent_news_articles")
public class SentNewsArticle {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "news_resource", nullable = false)
    @NonNull
    private RssService.RssResource newsResource;

    @Column(name = "article_id", nullable = false)
    @NonNull
    private String articleId;
}
