package com.l1sk1sh.vladikbot.services.logging;

import com.l1sk1sh.vladikbot.models.FixedCache;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class MessageCache {
    private static final int SIZE = 1000;
    private final Map<Long, FixedCache<Long, CachedMessage>> cache = new HashMap<>();

    public CachedMessage putMessage(Message m) {
        if (!cache.containsKey(m.getGuild().getIdLong())) {
            cache.put(m.getGuild().getIdLong(), new FixedCache<>(SIZE));
        }
        return cache.get(m.getGuild().getIdLong()).put(m.getIdLong(), new CachedMessage(m));
    }

    public CachedMessage pullMessage(Guild guild, long messageId) {
        if (!cache.containsKey(guild.getIdLong())) {
            return null;
        }
        return cache.get(guild.getIdLong()).pull(messageId);
    }

    @SuppressWarnings("unused")
    public List<CachedMessage> getMessages(Guild guild, Predicate<CachedMessage> predicate) {
        if (!cache.containsKey(guild.getIdLong())) {
            return Collections.emptyList();
        }
        return cache.get(guild.getIdLong()).getValues().stream().filter(predicate).collect(Collectors.toList());
    }

    @Getter
    public static final class CachedMessage implements ISnowflake {
        @Getter(AccessLevel.NONE)
        private final long id;
        private final long channel, guild;
        private final String content, username, discriminator;
        private final User author;
        private final List<Attachment> attachments;

        private CachedMessage(Message message) {
            content = message.getContentRaw();
            id = message.getIdLong();
            author = message.getAuthor();
            username = message.getAuthor().getName();
            discriminator = message.getAuthor().getDiscriminator();
            channel = message.getChannel().getIdLong();
            guild = message.getGuild().getIdLong();
            attachments = message.getAttachments();
        }

        @Override
        public long getIdLong() {
            return id;
        }

        public TextChannel getTextChannel(Guild guild) {
            return guild.getTextChannelById(channel);
        }

        @SuppressWarnings("unused")
        public TextChannel getTextChannel(ShardManager shardManager) {
            if (guild == 0L) {
                return null;
            }
            Guild g = shardManager.getGuildById(guild);
            if (g == null) {
                return null;
            }
            return g.getTextChannelById(channel);
        }

        @SuppressWarnings("unused")
        public Guild getGuild(ShardManager shardManager) {
            if (guild == 0L) {
                return null;
            }
            return shardManager.getGuildById(guild);
        }
    }
}