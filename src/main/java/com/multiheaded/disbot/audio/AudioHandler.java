package com.multiheaded.disbot.audio;

import com.multiheaded.disbot.models.queue.FairQueue;
import com.multiheaded.disbot.settings.Constants;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import com.multiheaded.disbot.models.playlist.PlaylistLoader.Playlist;
import com.multiheaded.disbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class AudioHandler extends AudioEventAdapter implements AudioSendHandler {
    private final FairQueue<QueuedTrack> queue = new FairQueue<>();
    private final List<AudioTrack> defaultQueue = new LinkedList<>();
    private final Set<String> votes = new HashSet<>();

    private final PlayerManager manager;
    private final AudioPlayer audioPlayer;
    private final long guildId;

    private final Settings settings;

    private AudioFrame lastFrame;

    AudioHandler(PlayerManager manager, Guild guild, AudioPlayer player) {
        this.manager = manager;
        this.audioPlayer = player;
        this.guildId = guild.getIdLong();
        settings = SettingsManager.getInstance().getSettings();
    }

    public int addTrackToFront(QueuedTrack qtrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(qtrack.getTrack());
            return -1;
        } else {
            queue.addAt(0, qtrack);
            return 0;
        }
    }

    public int addTrack(QueuedTrack qtrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(qtrack.getTrack());
            return -1;
        } else
            return queue.add(qtrack);
    }

    public FairQueue<QueuedTrack> getQueue() {
        return queue;
    }

    public void stopAndClear() {
        queue.clear();
        defaultQueue.clear();
        audioPlayer.stopTrack();
        //current = null;
    }

    public boolean isMusicPlaying(JDA jda) {
        return guild(jda).getSelfMember().getVoiceState().inVoiceChannel() && audioPlayer.getPlayingTrack() != null;
    }

    public Set<String> getVotes() {
        return votes;
    }

    public AudioPlayer getPlayer() {
        return audioPlayer;
    }

    public long getRequester() {
        if (audioPlayer.getPlayingTrack() == null || audioPlayer.getPlayingTrack().getUserData(Long.class) == null)
            return 0;
        return audioPlayer.getPlayingTrack().getUserData(Long.class);
    }

    public boolean playFromDefault() {
        if (!defaultQueue.isEmpty()) {
            audioPlayer.playTrack(defaultQueue.remove(0));
            return true;
        }

        if (settings == null || settings.getDefaultPlaylist() == null) {
            return false;
        }

        Playlist playlist = manager.getBot().getPlaylistLoader().getPlaylist(settings.getDefaultPlaylist());
        if (playlist == null || playlist.getItems().isEmpty()) {
            return false;
        }

        playlist.loadTracks(manager, (at) ->
        {
            if (audioPlayer.getPlayingTrack() == null)
                audioPlayer.playTrack(at);
            else
                defaultQueue.add(at);
        }, () ->
        {
            if (playlist.getTracks().isEmpty() && settings.shouldLeaveChannel())
                manager.getBot().closeAudioConnection(guildId);
        });
        return true;
    }

    // Audio Events
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // if the track ended normally, and we're in repeat mode, re-add it to the queue
        if (endReason == AudioTrackEndReason.FINISHED && settings.shouldRepeat()) {
            queue.add(new QueuedTrack(track.makeClone(),
                    track.getUserData(Long.class) == null ? 0L : track.getUserData(Long.class)));
        }

        if (queue.isEmpty()) {
            if (!playFromDefault()) {
                manager.getBot().getNowPlayingHandler().onTrackUpdate(guildId, null, this);
                if (settings.shouldLeaveChannel()) {
                    manager.getBot().closeAudioConnection(guildId);
                }
            }
        } else {
            QueuedTrack queuedTrack = queue.pull();
            player.playTrack(queuedTrack.getTrack());
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        votes.clear();
        manager.getBot().getNowPlayingHandler().onTrackUpdate(guildId, track, this);
    }


    // Formatting
    public Message getNowPlaying(JDA jda) {
        if (isMusicPlaying(jda)) {
            Guild guild = guild(jda);
            AudioTrack track = audioPlayer.getPlayingTrack();
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.append(FormatUtil.filter(settings.getSuccessEmoji() + " **Now Playing in "
                    + guild.getSelfMember().getVoiceState().getChannel().getName() + "...**"));

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

            if (track instanceof YoutubeAudioTrack && settings.useNpImages()) {
                embedBuilder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
            }

            if (track.getInfo().author != null && !track.getInfo().author.isEmpty())
                embedBuilder.setFooter("Source: " + track.getInfo().author, null);

            double progress = (double) audioPlayer.getPlayingTrack().getPosition() / track.getDuration();
            embedBuilder.setDescription((audioPlayer.isPaused() ? Constants.PAUSE_EMOJI : Constants.PLAY_EMOJI)
                    + " " + FormatUtil.progressBar(progress)
                    + " `[" + FormatUtil.formatTime(track.getPosition()) + "/" + FormatUtil.formatTime(track.getDuration()) + "]` "
                    + FormatUtil.volumeIcon(audioPlayer.getVolume()));

            return messageBuilder.setEmbed(embedBuilder.build()).build();
        } else return null;
    }

    public Message getNoMusicPlaying(JDA jda) {
        Guild guild = guild(jda);
        return new MessageBuilder()
                .setContent(FormatUtil.filter(settings.getSuccessEmoji() + " **Now Playing...**"))
                .setEmbed(new EmbedBuilder()
                        .setTitle("No music playing")
                        .setDescription(Constants.STOP_EMOJI + " "
                                + FormatUtil.progressBar(-1) + " "
                                + FormatUtil.volumeIcon(audioPlayer.getVolume()))
                        .setColor(guild.getSelfMember().getColor())
                        .build()).build();
    }

    String getTopicFormat(JDA jda) {
        if (isMusicPlaying(jda)) {
            long userid = getRequester();
            AudioTrack track = audioPlayer.getPlayingTrack();
            String title = track.getInfo().title;
            if (title == null || title.equals("Unknown Title"))
                title = track.getInfo().uri;
            return "**" + title + "** [" + (userid == 0 ? "autoplay" : "<@" + userid + ">") + "]"
                    + "\n" + (audioPlayer.isPaused() ? Constants.PAUSE_EMOJI : Constants.PLAY_EMOJI) + " "
                    + "[" + FormatUtil.formatTime(track.getDuration()) + "] "
                    + FormatUtil.volumeIcon(audioPlayer.getVolume());
        } else return "No music playing " + Constants.STOP_EMOJI + " "
                + FormatUtil.volumeIcon(audioPlayer.getVolume());
    }

    // Audio Send Handler methods
    @Override
    public boolean canProvide() {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }

        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide();
        }

        byte[] data = lastFrame != null ? lastFrame.getData() : null;
        lastFrame = null;

        return data;
    }

    @Override
    public boolean isOpus() {
        return true;
    }


    // Private methods
    private Guild guild(JDA jda) {
        return jda.getGuildById(guildId);
    }
}
