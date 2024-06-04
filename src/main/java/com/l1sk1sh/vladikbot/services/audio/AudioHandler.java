package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.entity.Playlist;
import com.l1sk1sh.vladikbot.models.AudioRepeatMode;
import com.l1sk1sh.vladikbot.models.AudioRequestMetadata;
import com.l1sk1sh.vladikbot.models.queue.AbstractQueue;
import com.l1sk1sh.vladikbot.models.queue.QueueType;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import dev.lavalink.youtube.track.YoutubeAudioTrack;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * @author John Grosh
 */
@Slf4j
public class AudioHandler extends AudioEventAdapter implements AudioSendHandler {

    private final ScheduledExecutorService frontThreadPool;
    private final BotSettingsManager settings;
    private final GuildSettings guildSettings;
    private final PlaylistLoader playlistLoader;
    private final PlayerManager playerManager;
    @Getter
    private final AudioPlayer audioPlayer;

    private final List<AudioTrack> defaultQueue = new LinkedList<>();
    @Getter
    private final Set<Long> votes = new HashSet<>();

    private final long guildId;

    private AudioFrame lastFrame;
    @Getter
    private AbstractQueue<QueuedTrack> queue;

    AudioHandler(ScheduledExecutorService frontThreadPool, PlayerManager playerManager, Guild guild, AudioPlayer player,
                 BotSettingsManager settings, GuildSettings guildSettings, PlaylistLoader playlistLoader) {
        this.frontThreadPool = frontThreadPool;
        this.playerManager = playerManager;
        this.audioPlayer = player;
        this.guildId = guild.getIdLong();
        this.settings = settings;
        this.guildSettings = guildSettings;
        this.playlistLoader = playlistLoader;

        this.setQueueType(settings.get().getQueueType());
    }

    public void setQueueType(QueueType type) {
        queue = type.createInstance(queue);
    }

    public int addTrackToFront(QueuedTrack queuedTrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(queuedTrack.getTrack());
            return -1;
        } else {
            queue.addAt(0, queuedTrack);
            return 0;
        }
    }

    public int addTrack(QueuedTrack queuedTrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(queuedTrack.getTrack());
            return -1;
        } else {
            return queue.add(queuedTrack);
        }
    }

    public void stopAndClear() {
        queue.clear();
        defaultQueue.clear();
        audioPlayer.stopTrack();
    }

    public boolean isMusicPlaying(JDA jda) {
        return Objects.requireNonNull(Objects.requireNonNull(guild(jda).getSelfMember().getVoiceState())).inAudioChannel() && audioPlayer.getPlayingTrack() != null;
    }

    public AudioRequestMetadata getRequestMetadata() {
        if (audioPlayer.getPlayingTrack() == null || audioPlayer.getPlayingTrack().getUserData(Long.class) == null) {
            return AudioRequestMetadata.EMPTY;
        }
        AudioRequestMetadata rm = audioPlayer.getPlayingTrack().getUserData(AudioRequestMetadata.class);
        return rm == null ? AudioRequestMetadata.EMPTY : rm;
    }

    public boolean playFromDefault() {
        if (!defaultQueue.isEmpty()) {
            audioPlayer.playTrack(defaultQueue.remove(0));
            return true;
        }

        if (guildSettings == null || guildSettings.getDefaultPlaylist() == null) {
            return false;
        }

        Playlist playlist = playlistLoader.getPlaylist(guildSettings.getDefaultPlaylist());
        if (playlist == null || playlist.getItems().isEmpty()) {
            return false;
        }

        playlistLoader.loadTracksIntoPlaylist(
                playlist,
                playerManager,
                (audioTrack) -> {
                    if (audioPlayer.getPlayingTrack() == null) {
                        audioPlayer.playTrack(audioTrack);
                    } else {
                        defaultQueue.add(audioTrack);
                    }
                },
                () -> {
                    if (playlist.getTracks().isEmpty() && settings.get().isLeaveChannel()) {
                        closeAudioConnection();
                    }
                }
        );

        return true;
    }

    /* Audio Events */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        AudioRepeatMode repeatMode = settings.get().getRepeat();
        /* If the track ended normally, and we're in repeat mode, re-add it to the queue */
        if (endReason == AudioTrackEndReason.FINISHED && repeatMode != AudioRepeatMode.OFF) {
            QueuedTrack clone = new QueuedTrack(track.makeClone(), track.getUserData(AudioRequestMetadata.class));
            if (repeatMode == AudioRepeatMode.ALL) {
                queue.add(clone);
            } else {
                queue.addAt(0, clone);
            }
        }

        if (queue.isEmpty()) {
            if (!playFromDefault()) {
                if (settings.get().isLeaveChannel()) {
                    closeAudioConnection();
                }
                /* Un-pause, in the case when the player was paused and the track has been skipped.
                This is to prevent the player being paused next time it's being used. */
                player.setPaused(false);
            }
        } else {
            QueuedTrack queuedTrack = queue.pull();
            player.playTrack(queuedTrack.getTrack());
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        votes.clear();
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.warn("Track exception {}.", track.getInfo(), exception);
        super.onTrackException(player, track, exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        log.warn("Track stuck {}.", track.getInfo());
        super.onTrackStuck(player, track, thresholdMs);
    }

    /* Formatting of the message */
    public MessageCreateData getNowPlaying(JDA jda) {
        if (isMusicPlaying(jda)) {
            Guild guild = guild(jda);
            AudioTrack track = audioPlayer.getPlayingTrack();
            MessageCreateBuilder messageBuilder = new MessageCreateBuilder();
            messageBuilder.addContent(FormatUtils.filter(settings.get().getSuccessEmoji() + " **Now Playing in "
                    + Objects.requireNonNull(Objects.requireNonNull(guild.getSelfMember().getVoiceState()).getChannel()).getAsMention() + "...**"));

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(guild.getSelfMember().getColor());
            AudioRequestMetadata rm = getRequestMetadata();
            if (rm.getOwner() != 0L) {

                User user = guild.getJDA().getUserById(rm.getUser().getId());
                if (user == null) {
                    embedBuilder.setAuthor(rm.getUser().getUsername(),
                            null,
                            rm.getUser().getAvatar());
                } else {
                    embedBuilder.setAuthor(user.getName(),
                            null, user.getEffectiveAvatarUrl());
                }
            }

            try {
                embedBuilder.setTitle(track.getInfo().title, track.getInfo().uri);
            } catch (Exception e) {
                embedBuilder.setTitle(track.getInfo().title);
            }

            if (track instanceof YoutubeAudioTrack && settings.get().isNpImages()) {
                embedBuilder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
            }

            if (track.getInfo().author != null && !track.getInfo().author.isEmpty()) {
                embedBuilder.setFooter("Source: " + track.getInfo().author, null);
            }

            double progress = (double) audioPlayer.getPlayingTrack().getPosition() / track.getDuration();
            embedBuilder.setDescription(getStatusEmoji()
                    + " " + FormatUtils.progressBar(progress)
                    + " `[" + FormatUtils.formatTimeTillHours(track.getPosition()) + "/"
                    + FormatUtils.formatTimeTillHours(track.getDuration()) + "]` "
                    + FormatUtils.volumeIcon(audioPlayer.getVolume()));

            return messageBuilder.setEmbeds(embedBuilder.build()).build();
        } else {
            return null;
        }
    }

    public MessageCreateData getNoMusicPlaying(JDA jda) {
        Guild guild = guild(jda);
        return new MessageCreateBuilder()
                .setContent(FormatUtils.filter(settings.get().getSuccessEmoji() + " **Now Playing...**"))
                .setEmbeds(new EmbedBuilder()
                        .setTitle("No music playing")
                        .setDescription(Const.STOP_EMOJI + " "
                                + FormatUtils.progressBar(-1) + " "
                                + FormatUtils.volumeIcon(audioPlayer.getVolume()))
                        .setColor(guild.getSelfMember().getColor())
                        .build()).build();
    }

    private String getStatusEmoji() {
        return audioPlayer.isPaused() ? Const.PAUSE_EMOJI : Const.PLAY_EMOJI;
    }

    /* Audio Send Handler methods */
    @Override
    public boolean canProvide() {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }

        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }

        ByteBuffer data = null;
        if (lastFrame != null) {
            data = ByteBuffer.wrap(lastFrame.getData());
        }

        lastFrame = null;

        return data;
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    public void closeAudioConnection() {
        Guild guild = guild(VladikBot.jda());
        if (guild != null) {
            frontThreadPool.submit(() -> guild.getAudioManager().closeAudioConnection());
        }
    }

    private Guild guild(JDA jda) {
        return jda.getGuildById(guildId);
    }
}
