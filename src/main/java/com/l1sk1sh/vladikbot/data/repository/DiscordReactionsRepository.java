package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.DiscordEmoji;
import com.l1sk1sh.vladikbot.data.entity.DiscordReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Repository
public interface DiscordReactionsRepository extends JpaRepository<DiscordEmoji, Long> {

    @Query("SELECT rect " +
            "FROM DiscordReaction rect " +
            "JOIN DiscordMessage msg " +
            "ON rect.discordMessageId = msg.id " +
            "WHERE msg.channelId = :channelId " +
            "AND rect.emojiId > 0")
    List<DiscordReaction> getAllEmojisByChannelId(@Param("channelId") long channelId);

    @Query("SELECT rect " +
            "FROM DiscordReaction rect " +
            "JOIN DiscordMessage msg " +
            "ON rect.discordMessageId = msg.id " +
            "WHERE msg.channelId = :channelId " +
            "AND rect.emojiId > 0 " +
            "AND msg.createdTime >= :after")
    List<DiscordReaction> getAllByChannelIdAndAfter(@Param("channelId") long channelId, @Param("after") long after);
}
