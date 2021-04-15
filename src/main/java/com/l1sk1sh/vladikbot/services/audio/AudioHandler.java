package com.l1sk1sh.vladikbot.services.audio;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.entity.Playlist;
import com.l1sk1sh.vladikbot.models.queue.FairQueue;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * @author John Grosh
 */
public class AudioHandler extends AudioEventAdapter implements AudioSendHandler {

    private final JDA jda;
    private final ScheduledExecutorService frontThreadPool;
    private final BotSettingsManager settings;
    private final GuildSettings guildSettings;
    private final PlaylistLoader playlistLoader;
    private final PlayerManager playerManager;
    private final AudioPlayer audioPlayer;
    private final NowPlayingHandler nowPlayingHandler;

    private final FairQueue<QueuedTrack> queue = new FairQueue<>();
    private final List<AudioTrack> defaultQueue = new LinkedList<>();
    private final Set<Long> votes = new HashSet<>();

    private final long guildId;

    private AudioFrame lastFrame;

    AudioHandler(JDA jda, ScheduledExecutorService frontThreadPool, PlayerManager playerManager, Guild guild, AudioPlayer player,
                 BotSettingsManager settings, GuildSettings guildSettings, PlaylistLoader playlistLoader, NowPlayingHandler nowPlayingHandler) {
        this.jda = jda;
        this.frontThreadPool = frontThreadPool;
        this.playerManager = playerManager;
        this.audioPlayer = player;
        this.guildId = guild.getIdLong();
        this.settings = settings;
        this.guildSettings = guildSettings;
        this.playlistLoader = playlistLoader;
        this.nowPlayingHandler = nowPlayingHandler;
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

    public FairQueue<QueuedTrack> getQueue() {
        return queue;
    }

    public void stopAndClear() {
        queue.clear();
        defaultQueue.clear();
        audioPlayer.stopTrack();
    }

    public boolean isMusicPlaying(JDA jda) {
        return Objects.requireNonNull(Objects.requireNonNull(guild(jda).getSelfMember().getVoiceState())).inVoiceChannel() && audioPlayer.getPlayingTrack() != null;
    }

    public Set<Long> getVotes() {
        return votes;
    }

    public AudioPlayer getPlayer() {
        return audioPlayer;
    }

    public long getRequester() {
        if (audioPlayer.getPlayingTrack() == null || audioPlayer.getPlayingTrack().getUserData(Long.class) == null) {
            return 0;
        }
        return audioPlayer.getPlayingTrack().getUserData(Long.class);
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
        if (playlist == null || playlist.getItems().isEmpty() || (playlist.getItems() == null)) {
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
        /* If the track ended normally, and we're in repeat mode, re-add it to the queue */
        if (endReason == AudioTrackEndReason.FINISHED && settings.get().isRepeat()) {
            queue.add(new QueuedTrack(track.makeClone(),
                    track.getUserData(Long.class) == null ? 0L : track.getUserData(Long.class)));
        }

        if (queue.isEmpty()) {
            if (!playFromDefault()) {
                nowPlayingHandler.onTrackUpdate(guildId, null, this);
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
        nowPlayingHandler.onTrackUpdate(guildId, track, this);
    }

    /* Formatting of the message */
    public Message getNowPlaying(JDA jda) {
        if (isMusicPlaying(jda)) {
            Guild guild = guild(jda);
            AudioTrack track = audioPlayer.getPlayingTrack();
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.append(FormatUtils.filter(settings.get().getSuccessEmoji() + " **Now Playing in "
                    + Objects.requireNonNull(Objects.requireNonNull(guild.getSelfMember().getVoiceState()).getChannel()).getName() + "...**"));

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(guild.getSelfMember().getColor());
            if (getRequester() != 0) {

                User user = guild.getJDA().getUserById(getRequester());
                if (user == null) {
                    embedBuilder.setAuthor("Unknown (ID:" + getRequester() + ")",
                            null, null);
                } else {
                    embedBuilder.setAuthor(user.getName() + "#" + user.getDiscriminator(),
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
            embedBuilder.setDescription((audioPlayer.isPaused() ? Const.PAUSE_EMOJI : Const.PLAY_EMOJI)
                    + " " + FormatUtils.progressBar(progress)
                    + " `[" + FormatUtils.formatTimeTillHours(track.getPosition()) + "/"
                    + FormatUtils.formatTimeTillHours(track.getDuration()) + "]` "
                    + FormatUtils.volumeIcon(audioPlayer.getVolume()));

            return messageBuilder.setEmbed(embedBuilder.build()).build();
        } else {
            return null;
        }
    }

    public Message getNoMusicPlaying(JDA jda) {
        Guild guild = guild(jda);
        return new MessageBuilder()
                .setContent(FormatUtils.filter(settings.get().getSuccessEmoji() + " **Now Playing...**"))
                .setEmbed(new EmbedBuilder()
                        .setTitle("No music playing")
                        .setDescription(Const.STOP_EMOJI + " "
                                + FormatUtils.progressBar(-1) + " "
                                + FormatUtils.volumeIcon(audioPlayer.getVolume()))
                        .setColor(guild.getSelfMember().getColor())
                        .build()).build();
    }

    String getTopicFormat(JDA jda) {
        if (isMusicPlaying(jda)) {
            long userId = getRequester();
            AudioTrack track = audioPlayer.getPlayingTrack();
            String title = track.getInfo().title;

            if (title == null || title.equals("Unknown Title")) {
                title = track.getInfo().uri;
            }
            return "**" + title + "** [" + (userId == 0 ? "autoplay" : "<@" + userId + ">") + "]"
                    + "\r\n" + (audioPlayer.isPaused() ? Const.PAUSE_EMOJI : Const.PLAY_EMOJI) + " "
                    + "[" + FormatUtils.formatTimeTillHours(track.getDuration()) + "] "
                    + FormatUtils.volumeIcon(audioPlayer.getVolume());
        } else {
            return "No music playing " + Const.STOP_EMOJI + " "
                    + FormatUtils.volumeIcon(audioPlayer.getVolume());
        }
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
        Guild guild = guild(jda);
        if (guild != null) {
            frontThreadPool.submit(() -> guild.getAudioManager().closeAudioConnection());
        }
    }

    private Guild guild(JDA jda) {
        return jda.getGuildById(guildId);
    }
}
