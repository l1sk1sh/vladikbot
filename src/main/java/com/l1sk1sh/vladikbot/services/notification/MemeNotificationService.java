package com.l1sk1sh.vladikbot.services.notification;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.network.dto.Meme;
import com.l1sk1sh.vladikbot.settings.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MemeNotificationService {

    private final GuildSettingsRepository guildSettingsRepository;

    public final void sendMemesArticle(@NotNull Guild guild, Meme meme) {
        TextChannel memeChannel = guildSettingsRepository.findById(guild.getIdLong()).map(guildSettings -> guildSettings.getMemesChannel(guild)).orElse(null);

        if (memeChannel == null) {
            log.warn("Meme channel is not set.");
            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Const.MEME_COLOR)
                .setTitle(meme.getTitle(), meme.getPostLink())
                .setImage(meme.getUrl())
                .setFooter(
                        String.format("%1$s", meme.getSubreddit()),
                        "https://www.redditstatic.com/icon.png"
                );

        memeChannel.sendMessage(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
