package com.l1sk1sh.vladikbot.utils;

import com.l1sk1sh.vladikbot.data.entity.DiscordAttachment;
import com.l1sk1sh.vladikbot.data.entity.DiscordEmoji;
import com.l1sk1sh.vladikbot.data.entity.DiscordMessage;
import com.l1sk1sh.vladikbot.data.entity.DiscordReaction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;

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
                message.getTimeCreated().toEpochSecond() * 1000,
                message.getMentions().getCustomEmojis().stream().map(emoji ->
                        mapEmojiToDiscordEmoji(emoji, message.getIdLong())
                ).collect(Collectors.toList()),
                message.getReactions().stream().map(reaction ->
                        mapMessageReactionToDiscordReaction(reaction, message.getIdLong())
                ).collect(Collectors.toList()),
                message.getAttachments().stream().map(attachment ->
                        mapMessageAttachmentToDiscordAttachment(attachment, message.getIdLong())
                ).collect(Collectors.toList())
        );
    }

    public static DiscordEmoji mapEmojiToDiscordEmoji(CustomEmoji emoji, long messageId) {
        return new DiscordEmoji(
                messageId,
                emoji.getIdLong(),
                emoji.getName(),
                emoji.getAsMention(),
                emoji.getImageUrl()
        );
    }

    public static DiscordReaction mapMessageReactionToDiscordReaction(MessageReaction reaction, long messageId) {
        return new DiscordReaction(
                messageId,
                reaction.getEmoji().getType().equals(Emoji.Type.CUSTOM)
                        ? String.valueOf(reaction.getEmoji().asCustom().getIdLong())
                        : reaction.getEmoji().asUnicode().getAsCodepoints(),
                reaction.getEmoji().getName()
        );
    }

    public static DiscordAttachment mapMessageAttachmentToDiscordAttachment(Message.Attachment attachment, long messageId) {
        return new DiscordAttachment(
                attachment.getIdLong(),
                messageId,
                attachment.getUrl(),
                attachment.getFileName(),
                attachment.getContentType(),
                false,
                false
        );
    }
}
