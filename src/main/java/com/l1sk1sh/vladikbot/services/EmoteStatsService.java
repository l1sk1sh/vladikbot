package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.data.entity.DiscordEmote;
import com.l1sk1sh.vladikbot.data.entity.DiscordMessage;
import com.l1sk1sh.vladikbot.data.entity.DiscordReaction;
import com.l1sk1sh.vladikbot.data.repository.DiscordEmotesRepository;
import com.l1sk1sh.vladikbot.data.repository.DiscordMessagesRepository;
import com.l1sk1sh.vladikbot.data.repository.DiscordReactionsRepository;
import com.l1sk1sh.vladikbot.models.EmoteStatsRecord;
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
public class EmoteStatsService {

    private final BotSettingsManager settings;
    private final DiscordEmotesRepository discordEmotesRepository;
    private final DiscordMessagesRepository discordMessagesRepository;
    private final DiscordReactionsRepository discordReactionsRepository;

    @Autowired
    public EmoteStatsService(BotSettingsManager settings, DiscordEmotesRepository discordEmotesRepository,
                             DiscordMessagesRepository discordMessagesRepository, DiscordReactionsRepository discordReactionsRepository) {
        this.settings = settings;
        this.discordEmotesRepository = discordEmotesRepository;
        this.discordMessagesRepository = discordMessagesRepository;
        this.discordReactionsRepository = discordReactionsRepository;
    }

    public List<EmoteStatsRecord> getEmoteStatisticsByTotalUsageAmount(long channelId) {
        List<DiscordMessage> allMessages = discordMessagesRepository.getAllByChannelId(channelId);
        List<DiscordEmote> allEmotes = discordEmotesRepository.getAllByChannelId(channelId);
        List<DiscordReaction> allReactions = discordReactionsRepository.getAllEmotesByChannelId(channelId);

        return getEmoteStatisticsByTotalUsage(allMessages, allEmotes, allReactions);
    }

    public List<EmoteStatsRecord> getEmoteStatisticsByTotalUsageAmountSince(long channelId, long since) {
        List<DiscordMessage> allMessages = discordMessagesRepository.getByChannelIdAndCreatedTimeAfter(channelId, since);
        List<DiscordEmote> allEmotes = discordEmotesRepository.getAllByChannelIdAndAfter(channelId, since);
        List<DiscordReaction> allReactions = discordReactionsRepository.getAllByChannelIdAndAfter(channelId, since);

        return getEmoteStatisticsByTotalUsage(allMessages, allEmotes, allReactions);
    }

    public List<EmoteStatsRecord> getEmoteStatisticsByTotalUsage(List<DiscordMessage> allMessages, List<DiscordEmote> allEmotes, List<DiscordReaction> allReactions) {
        Map<EmoteStatsRecord, EmoteStatsRecord> result = new HashMap<>();

        List<String> joinedEmoteReactionNames = allEmotes.stream().map(DiscordEmote::getName).collect(Collectors.toList());
        joinedEmoteReactionNames.addAll(allReactions.stream().map(DiscordReaction::getName).collect(Collectors.toList()));
        for (String emoteName : joinedEmoteReactionNames) {
            EmoteStatsRecord temp = new EmoteStatsRecord();
            temp.setEmoteName(emoteName);

            if (result.containsKey(temp)) {
                result.get(temp).setAmount(result.get(temp).getAmount() + 1);
            } else {
                temp.setAmount(1);
                result.put(temp, temp);
            }
        }

        Map<AuthorEmoteCompoundKey, Integer> authorEmoteAmount = new HashMap<>();

        for (DiscordMessage message : allMessages) {
            List<AuthorEmoteCompoundKey> inMessageEmoteKeys = message.getEmotes().stream()
                    .map(emote -> new AuthorEmoteCompoundKey(message.getAuthorId(), emote.getName()))
                    .collect(Collectors.toList());

            for (AuthorEmoteCompoundKey key : inMessageEmoteKeys) {
                if (authorEmoteAmount.containsKey(key)) {
                    authorEmoteAmount.put(key, authorEmoteAmount.get(key) + 1);
                } else {
                    authorEmoteAmount.put(key, 1);
                }
            }

            List<AuthorEmoteCompoundKey> reactionsEmoteKeys = message.getReactions().stream()
                    .filter(reaction -> reaction.getEmoteId() != 0)
                    .map(reaction -> new AuthorEmoteCompoundKey(message.getAuthorId(), reaction.getName()))
                    .collect(Collectors.toList());

            for (AuthorEmoteCompoundKey key : reactionsEmoteKeys) {
                if (authorEmoteAmount.containsKey(key)) {
                    authorEmoteAmount.put(key, authorEmoteAmount.get(key) + 1);
                } else {
                    authorEmoteAmount.put(key, 1);
                }
            }
        }

        for (EmoteStatsRecord record : result.values()) {
            @SuppressWarnings("OptionalGetWithoutIsPresent") // This one is covered by table integrity
                    long mostActiveAuthorId = authorEmoteAmount.entrySet().stream()
                    .filter((entry) -> entry.getKey().getEmoteName().equals(record.getEmoteName()))
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .get().getKey().getAuthorId();
            record.setMostActiveUserId(mostActiveAuthorId);
        }

        return new ArrayList<>(result.values());
    }

    @Getter
    @AllArgsConstructor
    private static class AuthorEmoteCompoundKey {
        private final long authorId;
        private final String emoteName;

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof AuthorEmoteCompoundKey)
                    && ((AuthorEmoteCompoundKey) obj).getAuthorId() == this.getAuthorId()
                    && ((AuthorEmoteCompoundKey) obj).getEmoteName().equals(this.getEmoteName());
        }

        @Override
        public int hashCode() {
            return (int) (authorId + emoteName.hashCode());
        }
    }
}
