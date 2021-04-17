package com.l1sk1sh.vladikbot.services.notification;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.network.dto.Meme;
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
public class MemeNotificationService {

    private final JDA jda;
    private final BotSettingsManager settings;
    private final GuildSettingsRepository guildSettingsRepository;
    private TextChannel memeChannel;

    public final void sendMemesArticle(Guild guild, Meme meme) {
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

        this.memeChannel.sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

    private boolean isMemeChannelMissing(Guild guild) {
        Guild memeGuild = guild;

        /* In case this guild doesn't have news channel, sending notification to maintainer */
        if (memeGuild == null) {
            memeGuild = jda.getGuildById(settings.get().getMaintainerGuildId());
        }

        if (memeGuild == null) {
            return true;
        }

        Guild finalMemeGuild = memeGuild;
        guildSettingsRepository.findById(memeGuild.getIdLong()).ifPresent(
                settings -> this.memeChannel = settings.getMemesChannel(finalMemeGuild));

        return (this.memeChannel == null);
    }
}
