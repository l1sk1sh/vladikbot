package com.l1sk1sh.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.models.entities.ReactionRule;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Oliver Johnson
 */
public class AutoModerationManager {
    private static final Logger log = LoggerFactory.getLogger(AutoModerationManager.class);

    private final Bot bot;
    private final Gson gson;
    private String moderationRulesFolder;

    public AutoModerationManager(Bot bot) {
        this.bot = bot;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.moderationRulesFolder = bot.getBotSettings().getModerationRulesFolder();
    }

    public void moderate(Message message) {
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
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public List<ReactionRule> getRules() throws IOException {
        if (FileUtils.fileOrFolderIsAbsent(moderationRulesFolder)) {
            FileUtils.createFolders(moderationRulesFolder);
            
            return null;
        }
        
        File folder = new File(moderationRulesFolder);
        List<ReactionRule> rules = new ArrayList<>();

        if (folder.listFiles() == null) {
            return null;
        }

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (getRule(file.getName().replace("." + Const.FileType.json, "")) != null) {
                rules.add(getRule(file.getName().replace("." + Const.FileType.json, "")));
            }
        }

        return rules;
    }

    public ReactionRule getRule(String name) {
        try {
            if (FileUtils.fileOrFolderIsAbsent(moderationRulesFolder)) {
                FileUtils.createFolders(moderationRulesFolder);
                
                return null;
            } else {
                return gson.fromJson(new FileReader(moderationRulesFolder
                        + name + "." + Const.FileType.json), ReactionRule.class);
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void deleteRule(String name) throws IOException {
        FileUtils.deleteFile(moderationRulesFolder + name + "." + Const.FileType.json);
    }

    public void writeRule(ReactionRule rule) throws IOException {
        FileUtils.createFolderIfAbsent(moderationRulesFolder);

        log.debug("Adding rule {}", rule.toString());
        JsonWriter writer = new JsonWriter(
                new FileWriter(moderationRulesFolder + rule.getRuleName() + "." + Const.FileType.json));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(rule, rule.getClass(), writer);
        writer.close();
    }
}
