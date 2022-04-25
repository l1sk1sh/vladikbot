package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Repository
public interface GuildSettingsRepository extends JpaRepository<GuildSettings, Long> {

    List<GuildSettings> getAllBySendMemesIsTrue();

    List<GuildSettings> getAllBySendNewsIsTrue();

    List<GuildSettings> getAllByLogGuildChangesIsTrue();
}