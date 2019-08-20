package com.multiheaded.vladikbot.commands.dj;

import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.commands.music.MusicCommand;
import com.multiheaded.vladikbot.settings.GuildSettings;
import com.multiheaded.vladikbot.settings.GuildSettingsManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

import java.util.Objects;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
abstract class DJCommand extends MusicCommand {
    DJCommand(Bot bot) {
        super(bot);
        this.category = new Category("DJ", event ->
        {
            if (event.getAuthor().getId().equals(event.getClient().getOwnerId())) {
                return true;
            }
            if (event.getGuild() == null) {
                return true;
            }
            if (event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                return true;
            }

            /* Intentionally calling GuildSettingsManager instead of `bot` due to strange bug in help output */
            GuildSettings settings = (GuildSettings) new GuildSettingsManager().getSettings(event.getGuild());
            Role djRole = Objects.requireNonNull(settings).getDjRole(event.getGuild());
            return djRole != null && (event.getMember().getRoles().contains(djRole)
                    || djRole.getIdLong() == event.getGuild().getIdLong());
        });
    }
}
