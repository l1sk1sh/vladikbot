package com.l1sk1sh.vladikbot.services.notification;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * @author l1sk1sh
 */
@Service
public class ChatNotificationService {
    private final JDA jda;
    private final BotSettingsManager settings;
    private final GuildSettingsRepository guildSettingsRepository;
    private TextChannel notificationChannel;
    private Guild notificationGuild;

    @Autowired
    public ChatNotificationService(JDA jda, BotSettingsManager settings, GuildSettingsRepository guildSettingsRepository) {
        this.jda = jda;
        this.settings = settings;
        this.guildSettingsRepository = guildSettingsRepository;
    }

    @SuppressWarnings("unused")
    public final void sendRawMessage(Guild guild, String message) {
        if (isNotificationChannelMissing(guild)) {
            return;
        }

        notificationChannel.sendMessage(message).queue();
    }

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
        if (isNotificationChannelMissing(guild)) {
            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(color)
                .setDescription(message);

        this.notificationChannel.sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

    private boolean isNotificationChannelMissing(Guild guild) {
        this.notificationGuild = guild;

        /* In case this guild doesn't have notification channel, sending notification to maintainer */
        if (this.notificationGuild == null) {
            this.notificationGuild = jda.getGuildById(settings.get().getMaintainerGuildId());
        }

        if (this.notificationGuild == null) {
            return true;
        }

        guildSettingsRepository.findById(notificationGuild.getIdLong()).ifPresent(
                settings -> this.notificationChannel = settings.getNotificationChannel(notificationGuild));

        return (this.notificationChannel == null);
    }
}
