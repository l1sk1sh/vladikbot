package com.l1sk1sh.vladikbot.services;

import com.l1sk1sh.vladikbot.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * @author Oliver Johnson
 */
// TODO Make embedded notification
public class ChatNotificationService {
    private final Bot bot;

    public ChatNotificationService(Bot bot) {
        this.bot = bot;
    }

    public final void sendMessage(Guild guild, String message) {
        TextChannel notificationChannel = bot.getGuildSettings(guild).getNotificationChannel(guild);

        if (notificationChannel != null) {
            notificationChannel.sendMessage(message).queue();
        }
    }
}
