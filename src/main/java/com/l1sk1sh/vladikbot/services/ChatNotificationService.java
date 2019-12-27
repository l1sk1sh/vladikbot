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

    public ChatNotificationService(Bot bot) {
        this.bot = bot;
    }

    @SuppressWarnings("unused")
    public final void sendRawMessage(Guild guild, String message) {
        TextChannel notificationChannel = bot.getGuildSettings(guild).getNotificationChannel(guild);

        if (notificationChannel == null) {
            return;
        }

        notificationChannel.sendMessage(message).queue();
    }

    @SuppressWarnings("unused")
    public final void sendEmbeddedSuccess(Guild guild, String message) {
        TextChannel notificationChannel = bot.getGuildSettings(guild).getNotificationChannel(guild);

        if (notificationChannel == null) {
            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(25, 167, 23))
                .setDescription(message);

        notificationChannel.sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }

    public final void sendEmbeddedError(Guild guild, String message) {
        TextChannel notificationChannel = bot.getGuildSettings(guild).getNotificationChannel(guild);

        if (notificationChannel == null) {
            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(172, 25, 23))
                .setDescription(message);

        notificationChannel.sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}
