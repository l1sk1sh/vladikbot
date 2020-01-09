package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;

import java.util.Arrays;

/**
 * @author Oliver Johnson
 */
public class RollDiceCommand extends Command {

    public RollDiceCommand() {
        this.name = "dice";
        this.aliases = new String[]{"roll"};
        this.help = "Roll the dice (support optional size d4, d8, d10, d12, d20, d00)";
        this.arguments = "<size> <number of dice>";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyWarning("Please add dice size into arguments. Supported sizes: d4, d8, d10, d12, d20, d00");

            return;
        }

        String[] args = event.getArgs().split(" ");

        int diceSize;
        switch (args[0]) {
            case "d4":
                diceSize = 4;
                break;
            case "d8":
                diceSize = 8;
                break;
            case "d10":
                diceSize = 10;
                break;
            case "d12":
                diceSize = 12;
                break;
            case "d20":
                diceSize = 20;
                break;
            case "d00":
                diceSize = 100;
                break;
            default:
                event.replyWarning("Dice size must be one of [d4, d8, d10, d12, d20, d00]!");
                return;
        }

        int amountOfDices = 1;
        if (args.length >= 2) {
            try {
                amountOfDices = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                event.replyWarning(String.format("Do not use %1$s as amount of dices. Use numbers instead.", args[1]));
            }
        }

        int[] results = new int[amountOfDices];
        for (int i = 0; i < amountOfDices; i++) {
            results[i] = between(diceSize);
        }

        event.reply(String.format("%1$s you rolled %2$s", event.getAuthor().getAsMention(), Arrays.toString(results)));
    }

    private int between(int max) {
        int min = 1;
        return Bot.rand.nextInt(max - min + 1) + min;
    }
}
