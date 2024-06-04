package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.DiscordAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Repository
public interface DiscordAttachmentsRepository extends JpaRepository<DiscordAttachment, Long> {

    @Query("SELECT att " +
            "FROM DiscordAttachment att " +
            "JOIN DiscordMessage msg " +
            "ON att.discordMessageId = msg.id " +
            "WHERE msg.channelId = :channelId")
    List<DiscordAttachment> getAllByChannelId(@Param("channelId") long channelId);

    @Query("SELECT att " +
            "FROM DiscordAttachment att " +
            "JOIN DiscordMessage msg " +
            "ON att.discordMessageId = msg.id " +
            "WHERE msg.channelId = :channelId " +
            "AND att.downloaded = false " +
            "AND att.downloadFailed = false")
    List<DiscordAttachment> getAllNotDownloadedAndNotFailedByChannelId(@Param("channelId") long channelId);

    @Query("UPDATE DiscordAttachment SET downloaded = false")
    void resetAllDownloadedAttachments();
}
