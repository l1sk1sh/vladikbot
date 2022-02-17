package com.l1sk1sh.vladikbot.data.repository;

import com.l1sk1sh.vladikbot.data.entity.DiscordMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author l1sk1sh
 */
@Repository
public interface DiscordMessagesRepository extends CrudRepository<DiscordMessage, Long> {

    List<DiscordMessage> getAllByChannelId(long channelId);

    List<DiscordMessage> getByChannelIdAndCreatedTimeAfter(long channelId, long after);
}
