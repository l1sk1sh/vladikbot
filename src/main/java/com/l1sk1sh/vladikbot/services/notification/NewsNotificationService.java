package com.l1sk1sh.vladikbot.services.notification;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.NewsDiscordMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * @author l1sk1sh
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NewsNotificationService {

    private final GuildSettingsRepository guildSettingsRepository;

    public final void sendNewsArticle(@NotNull Guild guild, NewsDiscordMessage message, Color color, NewsStyle style) {
        TextChannel newsChannel = guildSettingsRepository.findById(guild.getIdLong()).map(guildSettings -> guildSettings.getNewsChannel(guild)).orElse(null);

        if (newsChannel == null) {
            log.warn("News channel is not set.");
            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        EmbedBuilder embedBuilder = switch (style) {
            case SHORT -> new EmbedBuilder()
                    .setTitle(message.getTitle(), message.getArticleUrl());

            /* Falls through */
            default -> new EmbedBuilder()
                    .setColor(color)
                    .setTitle(message.getTitle(), message.getArticleUrl())
                    .setDescription(message.getDescription())
                    .setImage(message.getImageUrl())
                    .setFooter(
                            String.format("%1$s", message.getPublicationDate().toString()),
                            message.getResourceImageUrl()
                    );
        };

        newsChannel.sendMessage(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }

    public enum NewsStyle {
        FULL,
        SHORT
    }
}
