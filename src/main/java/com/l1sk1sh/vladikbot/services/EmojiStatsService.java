package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.data.entity.DiscordEmoji;
import com.l1sk1sh.vladikbot.data.entity.DiscordMessage;
import com.l1sk1sh.vladikbot.data.entity.DiscordReaction;
import com.l1sk1sh.vladikbot.data.repository.DiscordEmojisRepository;
import com.l1sk1sh.vladikbot.data.repository.DiscordMessagesRepository;
import com.l1sk1sh.vladikbot.data.repository.DiscordReactionsRepository;
import com.l1sk1sh.vladikbot.models.EmojiStatsRecord;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class EmojiStatsService {

    private final BotSettingsManager settings;
    private final DiscordEmojisRepository discordEmojisRepository;
    private final DiscordMessagesRepository discordMessagesRepository;
    private final DiscordReactionsRepository discordReactionsRepository;

    @Autowired
    public EmojiStatsService(BotSettingsManager settings, DiscordEmojisRepository discordEmojisRepository,
                             DiscordMessagesRepository discordMessagesRepository, DiscordReactionsRepository discordReactionsRepository) {
        this.settings = settings;
        this.discordEmojisRepository = discordEmojisRepository;
        this.discordMessagesRepository = discordMessagesRepository;
        this.discordReactionsRepository = discordReactionsRepository;
    }

    public List<EmojiStatsRecord> getEmojiStatisticsByTotalUsageAmount(long channelId) {
        List<DiscordMessage> allMessages = discordMessagesRepository.getAllByChannelId(channelId);
        List<DiscordEmoji> allEmojis = discordEmojisRepository.getAllByChannelId(channelId);
        List<DiscordReaction> allReactions = discordReactionsRepository.getAllEmojisByChannelId(channelId);

        return getEmojiStatisticsByTotalUsage(allMessages, allEmojis, allReactions);
    }

    public List<EmojiStatsRecord> getEmojiStatisticsByTotalUsageAmountSince(long channelId, long since) {
        List<DiscordMessage> allMessages = discordMessagesRepository.getByChannelIdAndCreatedTimeAfter(channelId, since);
        List<DiscordEmoji> allEmojis = discordEmojisRepository.getAllByChannelIdAndAfter(channelId, since);
        List<DiscordReaction> allReactions = discordReactionsRepository.getAllByChannelIdAndAfter(channelId, since);

        return getEmojiStatisticsByTotalUsage(allMessages, allEmojis, allReactions);
    }

    public List<EmojiStatsRecord> getEmojiStatisticsByTotalUsage(List<DiscordMessage> allMessages, List<DiscordEmoji> allEmojis, List<DiscordReaction> allReactions) {
        Map<EmojiStatsRecord, EmojiStatsRecord> result = new HashMap<>();

        List<String> joinedEmojiReactionNames = allEmojis.stream().map(DiscordEmoji::getName).collect(Collectors.toList());
        joinedEmojiReactionNames.addAll(allReactions.stream().map(DiscordReaction::getName).collect(Collectors.toList()));
        for (String emojiName : joinedEmojiReactionNames) {
            EmojiStatsRecord temp = new EmojiStatsRecord();
            temp.setEmojiName(emojiName);

            if (result.containsKey(temp)) {
                result.get(temp).setAmount(result.get(temp).getAmount() + 1);
            } else {
                temp.setAmount(1);
                result.put(temp, temp);
            }
        }

        Map<AuthorEmojiCompoundKey, Integer> authorEmojiAmount = new HashMap<>();

        for (DiscordMessage message : allMessages) {
            List<AuthorEmojiCompoundKey> inMessageEmojiKeys = message.getEmojis().stream()
                    .map(emoji -> new AuthorEmojiCompoundKey(message.getAuthorId(), emoji.getName()))
                    .collect(Collectors.toList());

            for (AuthorEmojiCompoundKey key : inMessageEmojiKeys) {
                if (authorEmojiAmount.containsKey(key)) {
                    authorEmojiAmount.put(key, authorEmojiAmount.get(key) + 1);
                } else {
                    authorEmojiAmount.put(key, 1);
                }
            }

            List<AuthorEmojiCompoundKey> reactionsEmojiKeys = message.getReactions().stream()
                    .filter(reaction -> reaction.getEmojiId() != 0)
                    .map(reaction -> new AuthorEmojiCompoundKey(message.getAuthorId(), reaction.getName()))
                    .collect(Collectors.toList());

            for (AuthorEmojiCompoundKey key : reactionsEmojiKeys) {
                if (authorEmojiAmount.containsKey(key)) {
                    authorEmojiAmount.put(key, authorEmojiAmount.get(key) + 1);
                } else {
                    authorEmojiAmount.put(key, 1);
                }
            }
        }

        for (EmojiStatsRecord record : result.values()) {
            @SuppressWarnings("OptionalGetWithoutIsPresent") // This one is covered by table integrity
                    long mostActiveAuthorId = authorEmojiAmount.entrySet().stream()
                    .filter((entry) -> entry.getKey().getEmojiName().equals(record.getEmojiName()))
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .get().getKey().getAuthorId();
            record.setMostActiveUserId(mostActiveAuthorId);
        }

        return new ArrayList<>(result.values());
    }

    @Getter
    @AllArgsConstructor
    private static class AuthorEmojiCompoundKey {
        private final long authorId;
        private final String emojiName;

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof AuthorEmojiCompoundKey)
                    && ((AuthorEmojiCompoundKey) obj).getAuthorId() == this.getAuthorId()
                    && ((AuthorEmojiCompoundKey) obj).getEmojiName().equals(this.getEmojiName());
        }

        @Override
        public int hashCode() {
            return (int) (authorId + emojiName.hashCode());
        }
    }
}
