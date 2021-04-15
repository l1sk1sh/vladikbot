package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.SentNewsArticle;
import com.l1sk1sh.vladikbot.services.rss.RssService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Oliver Johnson
 */
@Repository
public interface SentNewsArticleRepository extends JpaRepository<SentNewsArticle, Long> {
    List<SentNewsArticle> findTop20ByNewsResourceOrderByIdDesc(RssService.RssResource resource);
}
