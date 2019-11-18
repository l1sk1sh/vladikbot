package com.multiheaded.vladikbot.services;

import com.multiheaded.vladikbot.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Johnson
 */
public class ChatNotificationService {
    private Bot bot;

    public ChatNotificationService(Bot bot) {
        this.bot = bot;
    }

    public void sendMessage(Guild guild, String message) {
        TextChannel notificationChannel = bot.getGuildSettings(guild).getNotificationChannel(guild);

        if (notificationChannel != null) {
            notificationChannel.sendMessage(message).queue();
        }
    }
}
