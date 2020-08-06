package com.l1sk1sh.vladikbot.services.notification;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.Meme;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

/**
 * @author Oliver Johnson
 */
public class MemeNotificationService {
    private final Bot bot;
    private TextChannel newsChannel;

    public MemeNotificationService(Bot bot) {
        this.bot = bot;
    }

    public final void sendNewsArticle(Guild guild, Meme meme) {
        if (isMemeChannelMissing(guild)) {
            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(255, 69, 0))
                .setTitle(meme.getTitle(), meme.getPostLink())
                .setImage(meme.getUrl())
                .setFooter(
                        String.format("%1$s", meme.getSubreddit()),
                        "https://www.redditstatic.com/icon.png"
                );

        this.newsChannel.sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

    private boolean isMemeChannelMissing(Guild guild) {
        Guild memeGuild = guild;

        /* In case this guild doesn't have news channel, sending notification to maintainer */
        if (memeGuild == null) {
            memeGuild = bot.getJDA().getGuildById(bot.getBotSettings().getMaintainerGuildId());
        }

        if (memeGuild == null) {
            return true;
        }

        this.newsChannel = bot.getGuildSettings(memeGuild).getMemesChannel(memeGuild);

        return (this.newsChannel == null);
    }
}
