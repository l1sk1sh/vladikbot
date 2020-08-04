package com.l1sk1sh.vladikbot.services.notification;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.entities.NewsMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

/**
 * @author Oliver Johnson
 */
public class NewsNotificationService {
    private final Bot bot;
    private TextChannel newsChannel;

    public NewsNotificationService(Bot bot) {
        this.bot = bot;
    }

    public final void sendNewsArticle(Guild guild, NewsMessage message, Color color) {
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
            newsGuild = bot.getJDA().getGuildById(bot.getBotSettings().getMaintainerGuildId());
        }

        if (newsGuild == null) {
            return true;
        }

        this.newsChannel = bot.getGuildSettings(newsGuild).getNewsChannel(newsGuild);

        return (this.newsChannel == null);
    }
}
