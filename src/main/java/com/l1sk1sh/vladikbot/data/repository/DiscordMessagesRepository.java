package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.DiscordMessage;
import com.l1sk1sh.vladikbot.models.MessageStatsRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author l1sk1sh
 */
@SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection")
@Repository
public interface DiscordMessagesRepository extends CrudRepository<DiscordMessage, Long> {

    List<DiscordMessage> getAllByChannelId(long channelId);

    List<DiscordMessage> getByChannelIdAndCreatedTimeAfter(long channelId, long after);

    @Query("SELECT new com.l1sk1sh.vladikbot.models.MessageStatsRecord(d.content, count(d.content)) " +
            "FROM DiscordMessage AS d " +
            "GROUP BY d.content " +
            "ORDER BY 2 DESC")
    List<MessageStatsRecord> getTopMessages();

    @Query("SELECT new com.l1sk1sh.vladikbot.models.MessageStatsRecord(d.content, count(d.content), d.authorId) " +
            "FROM DiscordMessage AS d " +
            "GROUP BY d.content, d.authorId " +
            "ORDER BY 2 DESC")
    List<MessageStatsRecord> getTopMessagesGroupByAuthorId();

    @Query("SELECT new com.l1sk1sh.vladikbot.models.MessageStatsRecord(d.content, count(d.content), d.authorId) " +
            "FROM DiscordMessage AS d " +
            "WHERE d.channelId = :channelId " +
            "GROUP BY d.content, d.authorId " +
            "ORDER BY 2 DESC")
    List<MessageStatsRecord> getTopMessagesByChannelIdGroupByAuthorId(@Param("channelId") long channelId);

    @Query("SELECT new com.l1sk1sh.vladikbot.models.MessageStatsRecord(d.content, count(d.content), d.authorId) " +
            "FROM DiscordMessage AS d " +
            "WHERE d.channelId = :channelId " +
            "AND d.createdTime >= :since " +
            "GROUP BY d.content, d.authorId " +
            "ORDER BY 2 DESC")
    List<MessageStatsRecord> getTopMessagesByChannelIdGroupByAuthorIdSince(@Param("channelId") long channelId, @Param("since") long since);

    @Query("SELECT new com.l1sk1sh.vladikbot.models.MessageStatsRecord(d.content, count(d.content)) " +
            "FROM DiscordMessage AS d " +
            "JOIN DiscordReaction AS dr ON d.id = dr.discordMessageId " +
            "GROUP BY d.content " +
            "ORDER BY 2 DESC")
    List<MessageStatsRecord> getTopMessagesByReactions();
}
