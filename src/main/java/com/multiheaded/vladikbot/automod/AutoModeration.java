package com.multiheaded.vladikbot.automod;

import com.multiheaded.vladikbot.VladikBot;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;

public class AutoModeration {
    private static final String[] ACTION_WORDS = new String[]{"хуй", "гей", "нигер"};
    private static final String[] UBISOFT = new String[]{"ubisoft", "юбисофт"};
    private static final String[] MURKA = new String[]{"мурку", "мурка"};

    private final VladikBot bot;

    public AutoModeration(VladikBot bot) {
        this.bot = bot;
    }

    public void performAutomod(Message message) {
        if (Arrays.stream(ACTION_WORDS).parallel().anyMatch(message.toString()::contains)) {
            message.getChannel().sendMessage("Дааа, детка").queue();
        }

        if (Arrays.stream(UBISOFT).parallel().anyMatch(message.toString()::contains)) {
            message.getChannel().sendMessage("А что сразу Ubisoft, а?").queue();
        }

        if (Arrays.stream(MURKA).parallel().anyMatch(message.toString()::contains)) {
            message.getChannel().sendMessage("Сам давай https://www.youtube.com/watch?v=zC7fx_gQD_M").queue();
        }
    }
}
