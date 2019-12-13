package com.l1sk1sh.vladikbot.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.Quote;
import com.l1sk1sh.vladikbot.models.entities.ReplyRule;
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
public class AutoReplyManager {
    private static final Logger log = LoggerFactory.getLogger(AutoReplyManager.class);

    private static final String REPLY_RULES_JSON = "replies.json";

    private final Bot bot;
    private final Gson gson;
    private String rulesFolder;
    private List<ReplyRule> replyRules;

    public AutoReplyManager(Bot bot) {
        this.bot = bot;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.rulesFolder = bot.getBotSettings().getRulesFolder();
        this.replyRules = new ArrayList<>();
    }

    public void reply(Message message) {
        Random rand = new Random();
        if (message.getMentionedMembers().contains(message.getGuild().getSelfMember())) {
            try {
                Quote quote = bot.getRandomQuoteRetriever().call();
                message.getTextChannel().sendMessage(String.format("\"%1$s\" %2$s",
                        quote.getContent(),
                        quote.getAuthor())).queue();
            } catch (IOException e) {
                log.error("Failed to retrieve random quote for reply to mention:", e);
                message.getTextChannel().sendMessage("\"Кто буйный — ты буйный, ёпту бля!\" Интересная личность").queue();
            }

            return;
        }

        List<ReplyRule> matchingRules = new ArrayList<>();
        ReplyRule chosenRule;

        for (ReplyRule rule : replyRules) {
            if (Arrays.stream(rule.getReactToList().toArray(new String[0])).parallel()
                    .anyMatch(message.toString()::contains)) {
                matchingRules.add(rule);
            }
        }

        if (matchingRules.isEmpty()) {
            return;
        }

        if (matchingRules.size() > 1) {
            chosenRule = matchingRules.get(rand.nextInt(matchingRules.size()));
        } else {
            chosenRule = matchingRules.get(0);
        }

        message.getTextChannel().sendMessage(
                chosenRule.getReactWithList().get(
                        rand.nextInt(chosenRule.getReactWithList().size()))
        ).queue();
    }

    public ReplyRule getRuleById(int id) {
        for (ReplyRule rule : replyRules) {
            if (rule.getRuleId() == id) {

                return rule;
            }
        }

        return null;
    }

    public void writeRule(ReplyRule rule) throws IOException {
        log.debug("Writing new reply rule '{}'.", rule);

        if (getRuleById(rule.getRuleId()) != null) {
            log.info("Rule '{}' already exists. Removing...", rule.getRuleId());
            deleteRule(rule.getRuleId());
        }

        replyRules.add(rule);
        writeRules();
    }


    public void deleteRule(int id) throws IOException {
        ReplyRule rule = getRuleById(id);

        if (rule == null) {
            throw new IOException("Reply rule was not found");
        }

        log.info("Trying to remove reply rule '{}'...", rule);
        replyRules.remove(rule);
        writeRules();
    }

    public List<ReplyRule> getAllRules() throws IOException {
        if (replyRules.isEmpty()) {
            readRules();
        }

        return replyRules;
    }

    private void readRules() throws IOException {
        if (FileUtils.fileOrFolderIsAbsent(rulesFolder)) {
            FileUtils.createFolders(rulesFolder);

            return;
        }

        File folder = new File(rulesFolder);

        if (folder.listFiles() == null) {
            return;
        }

        File rulesFile = new File(rulesFolder + REPLY_RULES_JSON);

        if (!rulesFile.exists()) {
            return;
        }

        replyRules = gson.fromJson(new FileReader(rulesFile), new TypeToken<List<ReplyRule>>(){}.getType());
    }

    public void writeRules() throws IOException {
        File rulesFile = new File(rulesFolder + REPLY_RULES_JSON);
        JsonWriter writer = new JsonWriter(new FileWriter(rulesFile));
        writer.setIndent("  ");
        writer.setHtmlSafe(false);
        gson.toJson(replyRules, replyRules.getClass(), writer);
        writer.close();
    }
}
