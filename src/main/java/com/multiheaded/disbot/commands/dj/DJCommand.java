package com.multiheaded.disbot.commands.dj;

import com.multiheaded.disbot.Bot;
import com.multiheaded.disbot.commands.music.MusicCommand;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

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
            if (event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if (event.getGuild() == null)
                return true;
            if (event.getMember().hasPermission(Permission.MANAGE_SERVER))
                return true;
            Settings settings = SettingsManager.getInstance().getSettings();
            Role djRole = settings.getDjRole(event.getGuild());
            return djRole != null &&
                    (event.getMember().getRoles().contains(djRole) || djRole.getIdLong() == event.getGuild().getIdLong());
        });
    }
}
