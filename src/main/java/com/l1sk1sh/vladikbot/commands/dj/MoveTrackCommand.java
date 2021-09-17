package com.l1sk1sh.vladikbot.commands.dj;

import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.FairQueue;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Command that provides users the ability to move a track in the playlist.
 *
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class MoveTrackCommand extends DJCommand {

    private static final String FROM_OPTION_KEY = "from";
    private static final String TO_OPTION_KEY = "to";

    @Autowired
    public MoveTrackCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.name = "mmovetrack";
        this.help = "Move a track in the current queue to a different position";
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.INTEGER, FROM_OPTION_KEY, "From position").setRequired(true));
        options.add(new OptionData(OptionType.INTEGER, TO_OPTION_KEY, "To position").setRequired(true));
        this.options = options;
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(SlashCommandEvent event) {
        OptionMapping fromOption = event.getOption(FROM_OPTION_KEY);
        if (fromOption == null) {
            event.replyFormat("%1$s Please include from index", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        OptionMapping toOption = event.getOption(TO_OPTION_KEY);
        if (toOption == null) {
            event.replyFormat("%1$s Please include to index", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }


        int from = (int) fromOption.getAsLong();
        int to = (int) toOption.getAsLong();

        if (from == to) {
            event.replyFormat("%1$s Can't move a track to the same position", getClient().getWarning()).setEphemeral(true).queue();
            return;
        }

        /* Validate that 'from' and 'to' are available */
        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        FairQueue<QueuedTrack> queue = Objects.requireNonNull(handler).getQueue();
        if (isUnavailablePosition(queue, from)) {
            event.replyFormat("%1$s `%2$d` is not a valid position in the queue!", getClient().getError(), from).setEphemeral(true).queue();

            return;
        }

        if (isUnavailablePosition(queue, to)) {
            event.replyFormat("%1$s `%2$d` is not a valid position in the queue!", getClient().getError(), to).setEphemeral(true).queue();

            return;
        }

        /* Move the track */
        QueuedTrack track = queue.moveItem(from - 1, to - 1);
        event.replyFormat("Moved **%1$s** from position `%2$d` to `%3$d`.", track.getTrack().getInfo().title, from, to).queue();
    }

    private static boolean isUnavailablePosition(FairQueue<QueuedTrack> queue, int position) {
        return (position < 1 || position > queue.size());
    }
}
