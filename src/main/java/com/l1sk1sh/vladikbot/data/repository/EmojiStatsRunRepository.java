package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.EmojiStatsExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author l1sk1sh
 */
@Repository
public interface EmojiStatsRunRepository extends JpaRepository<EmojiStatsExecution, Long> {
    EmojiStatsExecution getLastRunByChannelId(Long channelId);
}
