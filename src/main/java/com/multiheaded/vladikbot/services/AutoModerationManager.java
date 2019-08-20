package com.multiheaded.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.multiheaded.vladikbot.Bot;
import com.multiheaded.vladikbot.models.entities.ReactionRule;
import com.multiheaded.vladikbot.settings.Constants;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.multiheaded.vladikbot.utils.FileUtils.*;

/**
 * @author Oliver Johnson
 */
public class AutoModerationManager {
    private static final Logger logger = LoggerFactory.getLogger(AutoModerationManager.class);

    private final Bot bot;
    private final String extension = Constants.JSON_EXTENSION;
    private final Gson gson;

    public AutoModerationManager(Bot bot) {
        this.bot = bot;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void performAutomod(Message message) {
        try {
            if (bot.getBotSettings().isAutoModeration()) {
                List<ReactionRule> allRules = getRules();

                for (ReactionRule rule : allRules) {
                    if (Arrays.stream(rule.getReactToList().toArray(new String[0])).parallel()
                            .anyMatch(message.toString()::contains)) {

                        message.getTextChannel().sendMessage(
                                rule.getReactWithList().get(
                                        new Random().nextInt(rule.getReactWithList().size()))
                        ).queue();
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public List<ReactionRule> getRules() throws IOException, NullPointerException {
        if (fileOrFolderIsAbsent(bot.getBotSettings().getModerationRulesFolder())) {
            createFolders(bot.getBotSettings().getModerationRulesFolder());
            return null;
        } else {
            File folder = new File(bot.getBotSettings().getModerationRulesFolder());
            List<ReactionRule> rules = new ArrayList<>();

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
            if (fileOrFolderIsAbsent(bot.getBotSettings().getModerationRulesFolder())) {
                createFolders(bot.getBotSettings().getModerationRulesFolder());
                return null;
            } else {
                return gson.fromJson(new FileReader(bot.getBotSettings().getModerationRulesFolder()
                        + name + extension), ReactionRule.class);
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void deleteRule(String name) throws IOException {
        deleteFile(bot.getBotSettings().getModerationRulesFolder() + name + extension);
    }

    public void writeRule(ReactionRule rule) throws IOException {
        if (fileOrFolderIsAbsent(bot.getBotSettings().getModerationRulesFolder())) {
            createFolders(bot.getBotSettings().getModerationRulesFolder());
            logger.info("Creating folder {}", bot.getBotSettings().getModerationRulesFolder());
        }

        logger.debug("Adding rule {}", rule.toString());
        JsonWriter writer = new JsonWriter(
                new FileWriter(bot.getBotSettings().getModerationRulesFolder() + rule.getRuleName() + extension));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(rule, rule.getClass(), writer);
        writer.close();
    }
}
