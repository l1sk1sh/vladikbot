package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.models.queue.QueuedTrack;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class RemoveCommand extends MusicCommand {

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public RemoveCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "remove";
        this.aliases = new String[]{"delete"};
        this.help = "removes a song from the queue";
        this.arguments = "<position|all>";
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (Objects.requireNonNull(audioHandler).getQueue().isEmpty()) {
            event.replyError("There is nothing in the queue!");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("all")) {
            int count = audioHandler.getQueue().removeAll(event.getAuthor().getIdLong());
            if (count == 0) {
                event.replyWarning("You don't have any songs in the queue!");
            } else {
                event.replySuccess(String.format("Successfully removed your %1$s entries.", count));
            }
            return;
        }

        int pos;
        try {
            pos = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            pos = 0;
        }

        if ((pos < 1) || (pos > audioHandler.getQueue().size())) {
            event.replyError(String.format("Position must be a valid integer between 1 and %1$s!",
                    audioHandler.getQueue().size()));
            return;
        }

        boolean isDJ = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if (!isDJ) {
            Optional<GuildSettings> guildSettings = guildSettingsRepository.findById(event.getGuild().getIdLong());
            isDJ = event.getMember().getRoles().contains(guildSettings.map(settings -> settings.getDjRole(event.getGuild())).orElse(null));
        }

        QueuedTrack queuedTrack = audioHandler.getQueue().get(pos - 1);
        if (queuedTrack.getIdentifier() == event.getAuthor().getIdLong()) {
            audioHandler.getQueue().remove(pos - 1);
            event.replySuccess(String.format("Removed **%1$s** from the queue", queuedTrack.getTrack().getInfo().title));
        } else if (isDJ) {
            audioHandler.getQueue().remove(pos - 1);
            User user;
            try {
                user = event.getJDA().getUserById(queuedTrack.getIdentifier());
            } catch (Exception e) {
                user = null;
            }
            event.replySuccess(String.format(
                    "Removed **%1$s** from the queue (requested by *%2$s*)",
                        queuedTrack.getTrack().getInfo().title,
                        ((user == null)
                                ? "someone"
                                : user.getName()))
            );
        } else {
            event.replyError(String.format("You cannot remove **%1$s** because you didn't add it!",
                    queuedTrack.getTrack().getInfo().title));
        }
    }
}
