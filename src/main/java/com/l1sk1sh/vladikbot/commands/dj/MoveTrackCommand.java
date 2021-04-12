package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.queue.FairQueue;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;

import java.util.Objects;

/**
 * Command that provides users the ability to move a track in the playlist.
 */
public class MoveTrackCommand extends DJCommand {
    public MoveTrackCommand(Bot bot) {
        super(bot);
        this.name = "movetrack";
        this.help = "move a track in the current queue to a different position";
        this.arguments = "<from> <to>";
        this.aliases = new String[]{"move"};
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(CommandEvent event) {
        int from;
        int to;

        String[] parts = event.getArgs().split("\\s+", 2);
        if (parts.length < 2) {
            event.replyError("Please include two valid indexes.");
            return;
        }

        try {
            /* Validate the args */
            from = Integer.parseInt(parts[0]);
            to = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            event.replyError("Please provide two valid indexes.");
            return;
        }

        if (from == to) {
            event.replyError("Can't move a track to the same position.");
            return;
        }

        /* Validate that from and to are available */
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        FairQueue<QueuedTrack> queue = Objects.requireNonNull(handler).getQueue();
        if (isUnavailablePosition(queue, from)) {
            String reply = String.format("`%d` is not a valid position in the queue!", from);
            event.replyError(reply);
            return;
        }
        if (isUnavailablePosition(queue, to)) {
            String reply = String.format("`%d` is not a valid position in the queue!", to);
            event.replyError(reply);
            return;
        }

        /* Move the track */
        QueuedTrack track = queue.moveItem(from - 1, to - 1);
        String trackTitle = track.getTrack().getInfo().title;
        String reply = String.format("Moved **%1$s** from position `%2$d` to `%3$d`.", trackTitle, from, to);
        event.replySuccess(reply);
    }

    private static boolean isUnavailablePosition(FairQueue<QueuedTrack> queue, int position) {
        return (position < 1 || position > queue.size());
    }
}
