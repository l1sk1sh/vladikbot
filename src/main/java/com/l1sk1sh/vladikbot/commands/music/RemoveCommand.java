package com.l1sk1sh.vladikbot.commands.music;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public class RemoveCommand extends MusicCommand {

    private static final String POSITION_OPTION_KEY = "position";
    private static final String POSITION_ALL_KEY = "all";

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public RemoveCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "mremove";
        this.help = "Removes a song from the queue";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, POSITION_OPTION_KEY, "Select specific song 'by number'. Use 'all' to clear queue").setRequired(false));
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if (Objects.requireNonNull(audioHandler).getQueue().isEmpty()) {
            event.replyFormat("%1$s  There is nothing in the queue!", getClient().getWarning()).setEphemeral(true).queue();
            return;
        }

        int position = 1;
        OptionMapping positionOption = event.getOption(POSITION_OPTION_KEY);
        if (positionOption != null) {
            if (positionOption.getAsString().equalsIgnoreCase(POSITION_ALL_KEY)) {
                int count = audioHandler.getQueue().removeAll(event.getUser().getIdLong());
                if (count == 0) {
                    event.replyFormat("%1$s You don't have any songs in the queue!", getClient().getWarning()).setEphemeral(true).queue();
                } else {
                    event.replyFormat("%1$s Successfully removed your %2$s entries.", getClient().getSuccess(), count).queue();
                }

                return;
            } else {
                try {
                    position = Integer.parseInt(positionOption.getAsString());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if ((position < 1) || (position > audioHandler.getQueue().size())) {
            event.replyFormat("%1$s Position must be a valid integer between 1 and %2$s!", getClient().getError(),
                    audioHandler.getQueue().size()).setEphemeral(true).queue();

            return;
        }

        boolean isDJ = Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER);
        if (!isDJ) {
            Optional<GuildSettings> guildSettings = guildSettingsRepository.findById(event.getGuild().getIdLong());
            isDJ = event.getMember().getRoles().contains(guildSettings.map(settings -> settings.getDjRole(event.getGuild())).orElse(null));
        }

        QueuedTrack queuedTrack = audioHandler.getQueue().get(position - 1);
        if (queuedTrack.getIdentifier() == event.getUser().getIdLong()) {
            audioHandler.getQueue().remove(position - 1);
            event.replyFormat("Removed **%1$s** from the queue", queuedTrack.getTrack().getInfo().title).queue();
        } else if (isDJ) {
            audioHandler.getQueue().remove(position - 1);
            User user;
            try {
                user = event.getJDA().getUserById(queuedTrack.getIdentifier());
            } catch (Exception e) {
                user = null;
            }
            event.replyFormat(
                    "%1$s Removed **%2$s** from the queue (requested by *%3$s*)",
                    getClient().getSuccess(),
                    queuedTrack.getTrack().getInfo().title,
                    ((user == null)
                            ? "someone"
                            : user.getName())
            ).queue();
        } else {
            event.replyFormat("%1$s You cannot remove **%2$s** because you didn't add it!", getClient().getError(),
                    queuedTrack.getTrack().getInfo().title).setEphemeral(true).queue();
        }
    }
}
