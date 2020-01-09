package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;

/**
 * @author Oliver Johnson
 */
public class FlipCoinCommand extends Command {

    public FlipCoinCommand() {
        this.name = "flip";
        this.help = "flip a coin";
    }

    @Override
    protected void execute(CommandEvent event) {
        int flipResult = Bot.rand.nextInt(2);

        if (flipResult == 1) {
            event.replySuccess("You flipped heads!");
        } else {
            event.replySuccess("You flipped tails!");
        }
    }
}
