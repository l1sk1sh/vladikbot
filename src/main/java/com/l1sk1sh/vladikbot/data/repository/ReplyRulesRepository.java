package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.ReplyReaction;
import com.l1sk1sh.vladikbot.data.entity.ReplyRule;
import com.l1sk1sh.vladikbot.data.entity.ReplyTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author l1sk1sh
 */
@Repository
public interface ReplyRulesRepository extends JpaRepository<ReplyRule, Long> {
    ReplyRule getByReactionAndTrigger(ReplyReaction reaction, ReplyTrigger trigger);
}
