package com.multiheaded.vladikbot;

import com.multiheaded.vladikbot.settings.Constants;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Removal of update
 * @author John Grosh
 */
class Listener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    private final Bot bot;

    Listener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuilds().isEmpty()) {
            logger.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            logger.warn(event.getJDA().asBot().getInviteUrl(Constants.RECOMMENDED_PERMS));
        }

        event.getJDA().getGuilds().forEach((guild) ->
        {
            try {
                String defaultPlaylist = bot.getGuildSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = bot.getGuildSettings(guild).getVoiceChannel(guild);
                if (defaultPlaylist != null && vc != null && bot.getPlayerManager().setUpHandler(guild).playFromDefault()) {
                    guild.getAudioManager().openAudioConnection(vc);
                }
            } catch (Exception ignore) {
            }
        });
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        bot.getNowPlayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (!message.getAuthor().isBot()) {
            bot.getAutoModerationManager().performAutomod(message);
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        bot.shutdown();
    }
}
