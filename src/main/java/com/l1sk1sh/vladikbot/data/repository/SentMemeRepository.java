package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.SentMeme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Repository
public interface SentMemeRepository extends JpaRepository<SentMeme, Long> {
    List<SentMeme> findTop30ByOrderByIdDesc();
}
