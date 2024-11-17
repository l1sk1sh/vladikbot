package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@RequiredArgsConstructor
@Service
public class PlayerManager extends DefaultAudioPlayerManager {

    @SuppressWarnings("SpringQualifierCopyableLombok") // See lombok.config
    @Qualifier("frontThreadPool")
    private final ScheduledExecutorService frontThreadPool;
    private final PlaylistLoader playlistLoader;
    private final BotSettingsManager settings;
    private final GuildSettingsRepository guildSettingsRepository;

    public final void init() {
        ClientOptions androidClientOptions = new ClientOptions();
        androidClientOptions.setPlayback(false);

        /* Consult https://github.com/lavalink-devs/youtube-source for details */
        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(true,
                new Music(),
                new Web(),
                new WebEmbedded(),
                new AndroidTestsuite(androidClientOptions),
                new AndroidLite(androidClientOptions),
                new AndroidMusic(androidClientOptions),
                new MediaConnect(),
                new Ios(),
                new TvHtml5Embedded());
        this.registerSourceManager(youtube);
        this.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    public final AudioHandler setUpHandler(Guild guild) {
        AudioHandler audioHandler;
        if (guild.getAudioManager().getSendingHandler() == null) {
            Optional<GuildSettings> guildSettings = guildSettingsRepository.findById(guild.getIdLong());
            int volume = guildSettings.map(GuildSettings::getVolume).orElse(50);

            AudioPlayer player = createPlayer();
            player.setVolume(volume);
            audioHandler = new AudioHandler(frontThreadPool, this, guild, player, settings, guildSettings.orElse(null), playlistLoader);
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
