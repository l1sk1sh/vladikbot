package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.api.Permission;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
abstract class AdminCommand extends Command {
    AdminCommand() {
        this.category = new Category("Admin", event ->
                event.getAuthor().getIdLong() == event.getClient().getOwnerIdLong()
                        || event.getGuild() == null
                        || event.getMember().hasPermission(Permission.MANAGE_CHANNEL));
        this.guildOnly = true;
    }
}
