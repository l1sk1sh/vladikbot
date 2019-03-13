package com.multiheaded.disbot.audio;

import com.multiheaded.disbot.models.queue.Queueable;
import com.multiheaded.disbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.User;

/**
 * @author John Grosh
 */
public class QueuedTrack implements Queueable {
    private final AudioTrack track;

    public QueuedTrack(AudioTrack track, User owner) {
        this(track, owner.getIdLong());
    }

    public QueuedTrack(AudioTrack track, long owner) {
        this.track = track;
        this.track.setUserData(owner);
    }

    @Override
    public long getIdentifier() {
        return track.getUserData(Long.class);
    }

    public AudioTrack getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return "`[" + FormatUtil.formatTime(track.getDuration()) + "]` **" + track.getInfo().title + "** - <@" + track.getUserData(Long.class) + ">";
    }
}
