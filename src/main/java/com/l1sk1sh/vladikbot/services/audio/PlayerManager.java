package com.l1sk1sh.vladikbot.services.audio;

import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.youtube.YouTubeIPRotator;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.*;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 *         Changes from original source:
 *         - Reformatted code
 *         - DI Spring
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
    private final YouTubeIPRotator ytIPRotator;

    public final void init() {
        this.registerSourceManager(SoundCloudAudioSourceManager.createDefault());

        initYoutube();
        initSpotify();
    }

    /* Consult https://github.com/lavalink-devs/youtube-source for details */
    private void initYoutube() {
        ClientOptions androidClientOptions = new ClientOptions();
        androidClientOptions.setPlayback(false);

        YoutubeSourceOptions options = new YoutubeSourceOptions()
                .setAllowSearch(true)
                .setAllowDirectVideoIds(true)
                .setAllowDirectPlaylistIds(true)
                .setRemoteCipher("https://cipher.kikkia.dev", null, null);

        YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(options,
                new Tv(),
                new Web(),
                new Music(),
                new WebEmbedded()
        // new AndroidMusic(androidClientOptions),
        // new AndroidVr(androidClientOptions),
        // new Ios()
        );

        // https://github.com/lavalink-devs/youtube-source?tab=readme-ov-file#using-oauth-tokens
        String refreshToken = settings.get().getYtRefreshToken();
        if (refreshToken != null && refreshToken.isEmpty()) {
            refreshToken = null;
        }
        youtube.useOauth2(refreshToken, refreshToken != null);

        // https://github.com/lavalink-devs/youtube-source/issues/13
        ytIPRotator.setRoutePlannerIfAvailable(youtube.getHttpInterfaceManager());

        this.registerSourceManager(youtube);
        source(YoutubeAudioSourceManager.class).setPlaylistPageCount(10);
    }

    /* Consult https://github.com/topi314/LavaSrc for details */
    private void initSpotify() {
        SpotifySourceManager spotify = new SpotifySourceManager(
                settings.get().getSpClientId(),
                settings.get().getSpClientSecret(),
                settings.get().getSpDc(),
                settings.get().getSpCountryCode(),
                (playerManager) -> this,
                new DefaultMirroringAudioTrackResolver(null));
        this.registerSourceManager(spotify);
    }

    public final AudioHandler setUpHandler(Guild guild) {
        AudioHandler audioHandler;
        if (guild.getAudioManager().getSendingHandler() == null) {
            Optional<GuildSettings> guildSettings = guildSettingsRepository.findById(guild.getIdLong());
            int volume = guildSettings.map(GuildSettings::getVolume).orElse(50);

            AudioPlayer player = createPlayer();
            player.setVolume(volume);
            audioHandler = new AudioHandler(frontThreadPool, this, guild, player, settings, guildSettings.orElse(null),
                    playlistLoader);
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
