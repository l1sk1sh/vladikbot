package com.multiheaded.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.models.entities.ReactionRule;
import com.multiheaded.vladikbot.settings.Constants;
import net.dv8tion.jda.core.entities.Message;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.multiheaded.vladikbot.utils.FileUtils.*;

/**
 * @author Oliver Johnson
 */
public class AutoModeration {
    private final VladikBot bot;
    private final String extension = Constants.JSON_EXTENSION;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AutoModeration(VladikBot bot) {
        this.bot = bot;
    }

    public void performAutomod(Message message) {
        if (bot.getSettings().isAutoModeration()) {
       /* if (Arrays.stream(UBISOFT).parallel().anyMatch(message.toString()::contains)) {
            message.getChannel().sendMessage("А что сразу Ubisoft, а?").queue();
        }*/
        }
    }

    public Set<ReactionRule> getRules() throws IOException, NullPointerException {
        if (fileOrFolderIsAbsent(bot.getSettings().getModerationRulesFolder())) {
            createFolder(bot.getSettings().getModerationRulesFolder());
            return null;
        } else {
            File folder = new File(bot.getSettings().getModerationRulesFolder());
            Set<ReactionRule> rules = new HashSet<>();

            if (folder.listFiles() == null) {
                return null;
            }

            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (getRule(file.getName().replace(extension, "")) != null) {
                    rules.add(getRule(file.getName().replace(extension, "")));
                }
            }
            return rules;
        }
    }

    public ReactionRule getRule(String name) {
        try {
            if (fileOrFolderIsAbsent(bot.getSettings().getModerationRulesFolder())) {
                createFolder(bot.getSettings().getModerationRulesFolder());
                return null;
            } else {
                return gson.fromJson(new FileReader(bot.getSettings().getModerationRulesFolder()
                        + name + extension), ReactionRule.class);
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void deleteRule(String name) throws IOException {
        deleteFile(bot.getSettings().getModerationRulesFolder() + name + extension);
    }

    public void writeRule(ReactionRule rule) throws IOException {
        if (fileOrFolderIsAbsent(bot.getSettings().getModerationRulesFolder())) {
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
