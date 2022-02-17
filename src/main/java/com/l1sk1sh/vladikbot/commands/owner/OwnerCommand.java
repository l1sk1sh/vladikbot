package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.SlashCommand;

/**
 * @author John Grosh
 */
abstract class OwnerCommand extends SlashCommand {
    OwnerCommand() {
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }
}
