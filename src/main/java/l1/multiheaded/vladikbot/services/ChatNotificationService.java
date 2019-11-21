package l1.multiheaded.vladikbot.services;

import l1.multiheaded.vladikbot.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * @author Oliver Johnson
 */
public class ChatNotificationService {
    private final Bot bot;

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
