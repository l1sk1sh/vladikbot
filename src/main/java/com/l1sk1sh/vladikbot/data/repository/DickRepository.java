package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.Dick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Repository
public interface DickRepository extends JpaRepository<Dick, Long> {

    Dick getDickByAuthorIdAndGuildId(long authorId, long guildId);

    List<Dick> getDicksByGuildId(long guildId);
}
