package com.multiheaded.disbot;

import com.multiheaded.disbot.settings.Constants;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
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
    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    private final Bot bot;
    private final Settings settings;

    Listener(Bot bot) {
        this.bot = bot;
        settings = SettingsManager.getInstance().getSettings();
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getGuilds().isEmpty()) {
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().asBot().getInviteUrl(Constants.RECOMMENDED_PERMS));
        }

        event.getJDA().getGuilds().forEach((guild) ->
        {
            try {
                String defpl = settings.getDefaultPlaylist();
                VoiceChannel vc = settings.getVoiceChannel(guild);
                if (defpl != null && vc != null && bot.getPlayerManager().setUpHandler(guild).playFromDefault()) {
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
    public void onShutdown(ShutdownEvent event) {
        bot.shutdown();
    }
}
