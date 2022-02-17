package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.DiscordEmote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Repository
public interface DiscordEmotesRepository extends JpaRepository<DiscordEmote, Long> {

    @Query("SELECT emt " +
            "FROM DiscordEmote emt " +
            "JOIN DiscordMessage msg " +
            "ON emt.discordMessageId = msg.id " +
            "WHERE msg.channelId = :channelId")
    List<DiscordEmote> getAllByChannelId(@Param("channelId") long channelId);

    @Query("SELECT emt " +
            "FROM DiscordEmote emt " +
            "JOIN DiscordMessage msg " +
            "ON emt.discordMessageId = msg.id " +
            "WHERE msg.channelId = :channelId " +
            "AND msg.createdTime >= :after")
    List<DiscordEmote> getAllByChannelIdAndAfter(@Param("channelId") long channelId, @Param("after") long after);
}
