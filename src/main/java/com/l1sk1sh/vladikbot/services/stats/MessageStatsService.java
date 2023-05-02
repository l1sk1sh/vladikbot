package com.l1sk1sh.vladikbot.services.stats;

import com.l1sk1sh.vladikbot.data.repository.DiscordMessagesRepository;
import com.l1sk1sh.vladikbot.models.MessageStatsRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class MessageStatsService {

    private final int MAX_SIZE = 50;

    private final DiscordMessagesRepository discordMessagesRepository;

    @Autowired
    public MessageStatsService(DiscordMessagesRepository discordMessagesRepository) {
        this.discordMessagesRepository = discordMessagesRepository;
    }

    public List<MessageStatsRecord> getMessageStatisticsByTotalUsageAmountPerAuthor(long channelId) {
        List<MessageStatsRecord> list = discordMessagesRepository.getTopMessagesByChannelIdGroupByAuthorId(channelId);
        return filterRecords(list);
    }

    public List<MessageStatsRecord> getMessageStatisticsByTotalUsageAmountPerAuthorSince(long channelId, long since) {
        List<MessageStatsRecord> list = discordMessagesRepository.getTopMessagesByChannelIdGroupByAuthorIdSince(channelId, since);
        return filterRecords(list);
    }

    public List<MessageStatsRecord> getMessageStatisticsByTotalUsageAmountPerAuthor() {
        List<MessageStatsRecord> list = discordMessagesRepository.getTopMessagesGroupByAuthorId();
        return filterRecords(list);
    }

    public List<MessageStatsRecord> getMessageStatisticsByTotalUsageAmount() {
        List<MessageStatsRecord> list = discordMessagesRepository.getTopMessages();
        return filterRecords(list);
    }

    public List<MessageStatsRecord> getMessageStatisticsByTotalReactions() {
        List<MessageStatsRecord> list = discordMessagesRepository.getTopMessagesByReactions();
        return filterRecords(list);
    }

    private List<MessageStatsRecord> filterRecords(List<MessageStatsRecord> input) {
        List<MessageStatsRecord> output = new ArrayList<>();
        for (MessageStatsRecord record : input) {
            String message = record.getMessage();
            if (message.isBlank()) {
                continue;
            }

            if (message.startsWith(":") && message.endsWith(":")) {
                continue;
            }

            output.add(record);

            if (output.size() >= MAX_SIZE) {
                break;
            }
        }

        return output;
    }
}
