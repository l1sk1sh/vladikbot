package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.commands.music.MusicCommand;
import com.l1sk1sh.vladikbot.settings.GuildSpecificSettings;
import com.l1sk1sh.vladikbot.settings.GuildSpecificSettingsManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

import java.util.Objects;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public abstract class DJCommand extends MusicCommand {
    DJCommand(Bot bot) {
        super(bot);
        this.category = new Category("DJ", DJCommand::checkDJPermission);
    }

    public static boolean checkDJPermission(CommandEvent event) {
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
        GuildSpecificSettings settings = new GuildSpecificSettingsManager().getSettings(event.getGuild());
        Role djRole = Objects.requireNonNull(settings).getDjRole(event.getGuild());
        return djRole != null && (event.getMember().getRoles().contains(djRole)
                || djRole.getIdLong() == event.getGuild().getIdLong());
    }
}
