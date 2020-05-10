package com.l1sk1sh.vladikbot.models.queue;

import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
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
    public final long getIdentifier() {
        return track.getUserData(Long.class);
    }

    public final AudioTrack getTrack() {
        return track;
    }

    @Override
    public final String toString() {
        return "`[" + FormatUtils.formatTimeTillHours(track.getDuration())
                + "]` **" + track.getInfo().title + "** - <@" + track.getUserData(Long.class) + ">";
    }
}
