package com.l1sk1sh.vladikbot.models.queue;

import com.l1sk1sh.vladikbot.models.AudioRequestMetadata;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * @author John Grosh
 */
public class QueuedTrack implements Queueable {

    @Getter
    private final AudioTrack track;

    public QueuedTrack(AudioTrack track, User owner) {
        this(track, new AudioRequestMetadata(owner));
    }

    public QueuedTrack(AudioTrack track, AudioRequestMetadata rm) {
        this.track = track;
        this.track.setUserData(rm);
    }

    @Override
    public final long getIdentifier() {
        return track.getUserData(AudioRequestMetadata.class).getOwner();
    }

    @Override
    public final String toString() {
        String entry = "`[" + FormatUtils.formatTimeTillHours(track.getDuration()) + "]` ";
        AudioTrackInfo trackInfo = track.getInfo();
        entry = entry + (trackInfo.uri.startsWith("http") ? "[**" + trackInfo.title + "**](" + trackInfo.uri + ")" : "**" + trackInfo.title + "**");
        return entry + " - <@" + track.getUserData(AudioRequestMetadata.class).getOwner() + ">";
    }
}
