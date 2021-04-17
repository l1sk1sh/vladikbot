package com.l1sk1sh.vladikbot.services.notification;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.NewsDiscordMessage;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * @author l1sk1sh
 */
@RequiredArgsConstructor
@Service
public class NewsNotificationService {

    private final JDA jda;
    private final BotSettingsManager settings;
    private final GuildSettingsRepository guildSettingsRepository;
    private TextChannel newsChannel;

    public final void sendNewsArticle(Guild guild, NewsDiscordMessage message, Color color) {
        if (isNewsChannelMissing(guild)) {
            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(color)
                .setTitle(message.getTitle(), message.getArticleUrl())
                .setDescription(message.getDescription())
                .setImage(message.getImageUrl())
                .setFooter(
                        String.format("%1$s", message.getPublicationDate().toString()),
                        message.getResourceImageUrl()
                );

        this.newsChannel.sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

    private boolean isNewsChannelMissing(Guild guild) {
        Guild newsGuild = guild;

        /* In case this guild doesn't have news channel, sending notification to maintainer */
        if (newsGuild == null) {
            newsGuild = jda.getGuildById(settings.get().getMaintainerGuildId());
        }

        if (newsGuild == null) {
            return true;
        }

        Guild finalNewsGuild = newsGuild;
        guildSettingsRepository.findById(finalNewsGuild.getIdLong()).ifPresent(
                settings -> this.newsChannel = settings.getNewsChannel(finalNewsGuild));

        return (this.newsChannel == null);
    }
}
