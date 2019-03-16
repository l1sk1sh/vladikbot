package com.multiheaded.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.core.Permission;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
abstract class AdminCommand extends Command {
    AdminCommand() {
        this.category = new Category("Admin", event ->
                event.getAuthor().getId()
                        .equals(event.getClient().getOwnerId())
                        || event.getGuild() == null
                        || event.getMember().hasPermission(Permission.MANAGE_CHANNEL));
        this.guildOnly = true;
    }
}
