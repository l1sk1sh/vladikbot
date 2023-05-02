package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.StatsExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author l1sk1sh
 */
@Repository
public interface StatsRunRepository extends JpaRepository<StatsExecution, Long> {
    StatsExecution getLastRunByChannelIdAndType(Long channelId, StatsExecution.Type type);
}
