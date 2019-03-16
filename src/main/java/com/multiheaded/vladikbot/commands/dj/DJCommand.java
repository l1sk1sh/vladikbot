package com.multiheaded.vladikbot.commands.dj;

import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.commands.music.MusicCommand;
import com.multiheaded.vladikbot.settings.Settings;
import com.multiheaded.vladikbot.settings.SettingsManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
abstract class DJCommand extends MusicCommand {
    DJCommand(VladikBot bot) {
        super(bot);
        this.category = new Category("DJ", event ->
        {
            if (event.getAuthor().getId().equals(event.getClient().getOwnerId()))
                return true;
            if (event.getGuild() == null)
                return true;
            if (event.getMember().hasPermission(Permission.MANAGE_CHANNEL))
                return true;
            Settings settings = SettingsManager.getInstance().getSettings();
            Role djRole = settings.getDjRole(event.getGuild());
            return djRole != null &&
                    (event.getMember().getRoles().contains(djRole) || djRole.getIdLong() == event.getGuild().getIdLong());
        });
    }
}
