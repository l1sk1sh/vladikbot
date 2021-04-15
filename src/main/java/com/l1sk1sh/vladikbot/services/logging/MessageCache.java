package com.l1sk1sh.vladikbot.services.logging;

import com.l1sk1sh.vladikbot.models.FixedCache;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class MessageCache {
    private final static int SIZE = 1000;
    private final HashMap<Long, FixedCache<Long, CachedMessage>> cache = new HashMap<>();

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

    public List<CachedMessage> getMessages(Guild guild, Predicate<CachedMessage> predicate) {
        if (!cache.containsKey(guild.getIdLong())) {
            return Collections.emptyList();
        }
        return cache.get(guild.getIdLong()).getValues().stream().filter(predicate).collect(Collectors.toList());
    }

    public static class CachedMessage implements ISnowflake {
        private final String content, username, discriminator;
        private final long id, channel, guild;
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

        public String getContentRaw() {
            return content;
        }

        public List<Attachment> getAttachments() {
            return attachments;
        }

        public User getAuthor() {
            return author;
        }

        public String getUsername() {
            return username;
        }

        public String getDiscriminator() {
            return discriminator;
        }

        public long getAuthorId() {
            return author.getIdLong();
        }

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

        public long getTextChannelId() {
            return channel;
        }

        public TextChannel getTextChannel(Guild guild) {
            return guild.getTextChannelById(channel);
        }

        public Guild getGuild(ShardManager shardManager) {
            if (guild == 0L) {
                return null;
            }
            return shardManager.getGuildById(guild);
        }

        @Override
        public long getIdLong() {
            return id;
        }
    }
}