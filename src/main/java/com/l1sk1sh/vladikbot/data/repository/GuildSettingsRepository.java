package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Oliver Johnson
 */
@Repository
public interface GuildSettingsRepository extends JpaRepository<GuildSettings, Long> {
}