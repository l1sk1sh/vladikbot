package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author l1sk1sh
 */
@Service
public class FlipCoinCommand extends SlashCommand {

    private final Random random;

    public FlipCoinCommand() {
        this.random = new Random();
        this.name = "flip";
        this.help = "Flip a coin";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        int flipResult = random.nextInt(2);

        if (flipResult == 1) {
            event.reply("You flipped heads!").queue();
        } else {
            event.reply("You flipped tails!").queue();
        }
    }
}
