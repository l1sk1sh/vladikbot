package com.l1sk1sh.vladikbot.services.notification;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * @author l1sk1sh
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatNotificationService {

    private final BotSettingsManager settings;
    private final GuildSettingsRepository guildSettingsRepository;

    public final void sendEmbeddedInfo(Guild guild, String message) {
        sendEmbeddedWithColor(guild, message, new Color(66, 133, 244));
    }

    @SuppressWarnings("unused")
    public final void sendEmbeddedSuccess(Guild guild, String message) {
        sendEmbeddedWithColor(guild, message, new Color(15, 157, 88));
    }

    public final void sendEmbeddedWarning(Guild guild, String message) {
        sendEmbeddedWithColor(guild, message, new Color(244, 180, 0));
    }

    public final void sendEmbeddedError(Guild guild, String message) {
        sendEmbeddedWithColor(guild, message, new Color(219, 68, 55));
    }

    private void sendEmbeddedWithColor(Guild guild, String message, Color color) {

        /* Falling back to maintainer guild for service messages */
        if (guild == null) {
            guild = VladikBot.jda().getGuildById(settings.get().getMaintainerGuildId());
        }

        if (guild == null) {
            log.warn("Set maintainer guild for service messages.");
            return;
        }

        Guild notificationGuild = guild;
        TextChannel notificationChannel = guildSettingsRepository.findById(notificationGuild.getIdLong()).map(guildSettings -> guildSettings.getNotificationChannel(notificationGuild)).orElse(null);

        if (notificationChannel == null) {
            log.warn("Guild '{}' doesn't have notification channel set.", guild.getName());
            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(color)
                .setDescription(message);

        notificationChannel.sendMessage(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
