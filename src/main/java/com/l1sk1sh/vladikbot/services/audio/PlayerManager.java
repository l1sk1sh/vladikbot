package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class PlayerManager extends DefaultAudioPlayerManager {

    private final JDA jda;
    private final ScheduledExecutorService frontThreadPool;
    private final PlaylistLoader playlistLoader;
    private final NowPlayingHandler nowPlayingHandler;
    private final BotSettingsManager settings;
    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public PlayerManager(JDA jda, @Qualifier("frontThreadPool") ScheduledExecutorService frontThreadPool, PlaylistLoader playlistLoader, NowPlayingHandler nowPlayingHandler,
                         BotSettingsManager settings, GuildSettingsRepository guildSettingsRepository) {
        this.jda = jda;
        this.frontThreadPool = frontThreadPool;
        this.playlistLoader = playlistLoader;
        this.nowPlayingHandler = nowPlayingHandler;
        this.settings = settings;
        this.guildSettingsRepository = guildSettingsRepository;
    }

    public final void init() {
        AudioSourceManagers.registerRemoteSources(this);
        AudioSourceManagers.registerLocalSource(this);
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    public final AudioHandler setUpHandler(Guild guild) {
        AudioHandler audioHandler;
        if (guild.getAudioManager().getSendingHandler() == null) {
            Optional<GuildSettings> guildSettings = guildSettingsRepository.findById(guild.getIdLong());
            int volume = guildSettings.map(GuildSettings::getVolume).orElse(50);

            AudioPlayer player = createPlayer();
            player.setVolume(volume);
            audioHandler = new AudioHandler(jda, frontThreadPool, this, guild, player, settings, guildSettings.orElse(null), playlistLoader, nowPlayingHandler);
            player.addListener(audioHandler);
            guild.getAudioManager().setSendingHandler(audioHandler);
        } else {
            audioHandler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        }

        return audioHandler;
    }

    public boolean hasHandler(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }
}
