package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.ReplyReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author l1sk1sh
 */
@Repository
public interface ReplyReactionsRepository extends JpaRepository<ReplyReaction, Long> {
    ReplyReaction findByReaction(String reaction);
}
