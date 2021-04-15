package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author l1sk1sh
 */
@Service
public class FlipCoinCommand extends Command {

    private final Random random;

    @Autowired
    public FlipCoinCommand() {
        this.random = new Random();
        this.name = "flip";
        this.help = "flip a coin";
    }

    @Override
    protected void execute(CommandEvent event) {
        int flipResult = random.nextInt(2);

        if (flipResult == 1) {
            event.replySuccess("You flipped heads!");
        } else {
            event.replySuccess("You flipped tails!");
        }
    }
}
