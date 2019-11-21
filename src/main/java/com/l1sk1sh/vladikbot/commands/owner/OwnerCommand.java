package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.Command;

/**
 * @author John Grosh
 */
abstract class OwnerCommand extends Command {
    OwnerCommand() {
        this.category = new Category("Owner");
        this.ownerCommand = true;
    }
}
