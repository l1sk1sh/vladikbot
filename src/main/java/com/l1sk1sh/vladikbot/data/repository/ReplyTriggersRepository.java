package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.ReplyTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author l1sk1sh
 */
@Repository
public interface ReplyTriggersRepository extends JpaRepository<ReplyTrigger, Long> {
    ReplyTrigger findByTrigger(String trigger);
}
