package com.l1sk1sh.vladikbot.utils;

import com.l1sk1sh.vladikbot.data.entity.DiscordAttachment;
import com.l1sk1sh.vladikbot.data.entity.DiscordEmote;
import com.l1sk1sh.vladikbot.data.entity.DiscordMessage;
import com.l1sk1sh.vladikbot.data.entity.DiscordReaction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.stream.Collectors;

/**
 * @author l1sk1sh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapperUtils {

    public static DiscordMessage mapMessageToDiscordMessage(Message message) {
        return new DiscordMessage(
                message.getIdLong(),
                message.getChannel().getIdLong(),
                message.getAuthor().getIdLong(),
                message.getContentStripped(),
                message.getTimeCreated().toEpochSecond(),
                message.getEmotes().stream().map(emote ->
                        mapEmoteToDiscordEmote(emote, message.getIdLong())
                ).collect(Collectors.toList()),
                message.getReactions().stream().map(reaction ->
                        mapMessageReactionToDiscordReaction(reaction, message.getIdLong())
                ).collect(Collectors.toList()),
                message.getAttachments().stream().map(attachment ->
                        mapMessageAttachmentToDiscordAttachment(attachment, message.getIdLong())
                ).collect(Collectors.toList())
        );
    }

    public static DiscordEmote mapEmoteToDiscordEmote(Emote emote, long messageId) {
        return new DiscordEmote(
                messageId,
                emote.getIdLong(),
                emote.isAvailable(),
                emote.getName(),
                emote.getAsMention(),
                emote.getImageUrl()
        );
    }

    public static DiscordReaction mapMessageReactionToDiscordReaction(MessageReaction reaction, long messageId) {
        return new DiscordReaction(
                messageId,
                (reaction.getReactionEmote().isEmoji())
                        ? 0
                        : reaction.getReactionEmote().getIdLong(),
                (reaction.getReactionEmote().isEmoji())
                        ? reaction.getReactionEmote().getAsCodepoints()
                        : reaction.getReactionEmote().getName()
        );
    }

    public static DiscordAttachment mapMessageAttachmentToDiscordAttachment(Message.Attachment attachment, long messageId) {
        return new DiscordAttachment(
                attachment.getIdLong(),
                messageId,
                attachment.getUrl(),
                attachment.getFileName(),
                attachment.getContentType(),
                false
        );
    }
}
