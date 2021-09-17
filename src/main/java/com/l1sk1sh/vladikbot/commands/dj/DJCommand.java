package com.l1sk1sh.vladikbot.commands.dj;

import com.l1sk1sh.vladikbot.commands.music.MusicCommand;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public abstract class DJCommand extends MusicCommand {

    @Autowired
    protected DJCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager) {
        super(guildSettingsRepository, playerManager);
    }

    public boolean checkDJPermission(SlashCommandEvent event) {
        if (event.getUser().getIdLong() == getClient().getOwnerIdLong()) {
            return true;
        }
        if (event.getGuild() == null) {
            return true;
        }
        if (Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) {
            return true;
        }

        Optional<GuildSettings> settings = guildSettingsRepository.findById(event.getGuild().getIdLong());
        Role djRole = settings.map(guildSettings -> guildSettings.getDjRole(event.getGuild())).orElse(null);
        return djRole != null && (event.getMember().getRoles().contains(djRole)
                || djRole.getIdLong() == event.getGuild().getIdLong());
    }
}
