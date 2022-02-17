package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.EmoteStatsRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author l1sk1sh
 */
@Repository
public interface EmoteStatsRunRepository extends JpaRepository<EmoteStatsRun, Long> {
    EmoteStatsRun getLastRunByChannelId(Long channelId);
}
