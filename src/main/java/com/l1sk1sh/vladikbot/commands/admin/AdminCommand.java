package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.Permission;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
abstract class AdminCommand extends SlashCommand {
    AdminCommand() {
        this.category = new Category("Admin", event ->
                event.getAuthor().getIdLong() == event.getClient().getOwnerIdLong()
                        || event.getGuild() == null
                        || event.getMember().hasPermission(Permission.MANAGE_CHANNEL));
        this.guildOnly = true;
    }
}
