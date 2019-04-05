package com.multiheaded.vladikbot.services.audio;

import com.multiheaded.vladikbot.VladikBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.core.entities.Guild;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class PlayerManager extends DefaultAudioPlayerManager {
    private final VladikBot bot;

    public PlayerManager(VladikBot bot) {
        this.bot = bot;
    }

    public void init() {
        AudioSourceManagers.registerRemoteSources(this);
        AudioSourceManagers.registerLocalSource(this);
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    public VladikBot getBot() {
        return bot;
    }

    public AudioHandler setUpHandler(Guild guild) {
        AudioHandler audioHandler;
        if (guild.getAudioManager().getSendingHandler() == null) {
            AudioPlayer player = createPlayer();
            player.setVolume(bot.getSettings().getVolume());
            audioHandler = new AudioHandler(this, guild, player, bot.getSettings());
            player.addListener(audioHandler);
            guild.getAudioManager().setSendingHandler(audioHandler);
        } else {
            audioHandler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        }
        return audioHandler;
    }
}
