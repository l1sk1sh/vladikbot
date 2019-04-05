package com.multiheaded.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.models.entities.ReactionRule;
import com.multiheaded.vladikbot.settings.Constants;
import net.dv8tion.jda.core.entities.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.multiheaded.vladikbot.utils.FileUtils.createFolder;
import static com.multiheaded.vladikbot.utils.FileUtils.fileIsAbsent;

/**
 * @author Oliver Johnson
 */
public class AutoModerationService {
    private final VladikBot bot;
    private final String extension = Constants.JSON_EXTENSION;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AutoModerationService(VladikBot bot) {
        this.bot = bot;
    }

    public void performAutomod(Message message) {
        if (bot.getSettings().isAutoModeration()) {
       /* if (Arrays.stream(UBISOFT).parallel().anyMatch(message.toString()::contains)) {
            message.getChannel().sendMessage("А что сразу Ubisoft, а?").queue();
        }*/
        }
    }

    public void writeRule(ReactionRule rule) throws IOException {
        if (fileIsAbsent(bot.getSettings().getModerationRulesFolder())) {
            createFolder(bot.getSettings().getModerationRulesFolder());
        }

        JsonWriter writer = new JsonWriter(
                new FileWriter(bot.getSettings().getModerationRulesFolder()
                        + File.separator + rule.getRuleName() + extension));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(rule, rule.getClass(), writer);
        writer.close();
    }
}
