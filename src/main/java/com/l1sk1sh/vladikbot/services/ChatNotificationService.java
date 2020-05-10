package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;

/**
 * @author Oliver Johnson
 */
public class ChatNotificationService {
    private final Bot bot;
    private TextChannel notificationChannel;
    private Guild notificationGuild;

    public ChatNotificationService(Bot bot) {
        this.bot = bot;
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

        notificationChannel.sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

    private boolean isNotificationChannelMissing(Guild guild) {
        if (guild == null) {

            /* In case this guild doesn't have notification channel, sending notification to maintainer */
            this.notificationGuild = bot.getJDA().getGuildById(bot.getBotSettings().getMaintainerGuildId());
        }

        if (this.notificationGuild == null) {
            return false;
        }

        this.notificationChannel = bot.getGuildSettings(guild).getNotificationChannel(guild);

        return notificationChannel == null;
    }
}
