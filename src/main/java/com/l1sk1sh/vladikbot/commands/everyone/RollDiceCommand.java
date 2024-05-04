package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author l1sk1sh
 */
@Service
public class RollDiceCommand extends SlashCommand {

    private static final String SIZE_OPTION_KEY = "size";
    private static final String AMOUNT_OPTION_KEY = "amount";

    private final Random random;

    public RollDiceCommand() {
        this.random = new Random();
        this.name = "dice";
        this.help = "Roll the dice (support optional size d4, d6, d8, d10, d12, d20, d00)";
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, SIZE_OPTION_KEY, "Size of the single dice").setRequired(true)
                .addChoice("4-sided dice", "d4")
                .addChoice("6-sided dice", "d6")
                .addChoice("8-sided dice", "d8")
                .addChoice("10-sided dice", "d10")
                .addChoice("12-sided dice", "d12")
                .addChoice("20-sided dice", "d20")
                .addChoice("100-sided dice", "d00")
        );
        options.add(new OptionData(OptionType.INTEGER, AMOUNT_OPTION_KEY, "Number of dices").setRequired(false));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping sizeOption = event.getOption(SIZE_OPTION_KEY);
        if (sizeOption == null) {
            event.replyFormat("%1$s Please add dice size into arguments. Supported sizes: d4, d6, d8, d10, d12, d20, d00", event.getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        int diceSize;
        switch (sizeOption.getAsString()) {
            case "d4":
            case "4":
                diceSize = 4;
                break;
            case "d6":
            case "6":
                diceSize = 6;
                break;
            case "d8":
            case "8":
                diceSize = 8;
                break;
            case "d10":
            case "10":
                diceSize = 10;
                break;
            case "d12":
            case "12":
                diceSize = 12;
                break;
            case "d20":
            case "20":
                diceSize = 20;
                break;
            case "d00":
            case "00":
            case "100":
                diceSize = 100;
                break;
            default:
                event.replyFormat("%1$s Dice size must be one of [d4, d6, d8, d10, d12, d20, d00]!", event.getClient().getWarning()).setEphemeral(true).queue();
                return;
        }

        int amountOfDices = 1;
        OptionMapping amountOption = event.getOption(AMOUNT_OPTION_KEY);
        if (amountOption != null) {
            amountOfDices = (int) amountOption.getAsLong();
        }

        int[] results = new int[amountOfDices];
        for (int i = 0; i < amountOfDices; i++) {
            results[i] = between(diceSize);
        }

        event.replyFormat("%1$s you rolled %2$s", event.getUser().getAsMention(), Arrays.toString(results)).queue();
    }

    private int between(int max) {
        int min = 1;
        return random.nextInt(max - min + 1) + min;
    }
}
