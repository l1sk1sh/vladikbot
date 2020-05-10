package com.l1sk1sh.vladikbot.services.presence;

import com.google.gson.reflect.TypeToken;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.Quote;
import com.l1sk1sh.vladikbot.models.entities.ReplyRule;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Oliver Johnson
 */
public class AutoReplyManager {
    private static final Logger log = LoggerFactory.getLogger(AutoReplyManager.class);

    private static final String REPLY_RULES_JSON = "replies.json";
    public static final int MIN_REPLY_TO_LENGTH = 3;

    private final Bot bot;
    private final String rulesFolder;
    private List<ReplyRule> replyRules;

    public AutoReplyManager(Bot bot) {
        this.bot = bot;
        this.rulesFolder = bot.getBotSettings().getRulesFolder();
        this.replyRules = new ArrayList<>();
    }

    public void reply(Message message) {
        if (replyRules.isEmpty()) {
            try {
                readRules();
                log.trace("Initial reading of rules to get reply.");
            } catch (IOException e) {
                log.error("Failed to read rules for auto reply:", e);
                bot.getNotificationService().sendEmbeddedError(message.getGuild(), "Failed to read rules for reply message.");
            }
        }

        if (message.getMentionedMembers().contains(message.getGuild().getSelfMember())) {
            try {
                Quote quote = bot.getRandomQuoteRetriever().call();
                message.getTextChannel().sendMessage(String.format("\"%1$s\" %2$s",
                        quote.getContent(),
                        quote.getAuthor())).queue();
            } catch (IOException e) {
                log.error("Failed to retrieve random quote for reply to mention:", e);
                message.getTextChannel().sendMessage("\"I'm sorry, I don't take orders. I barely take suggestions.\"").queue();
            }

            return;
        }

        /* Replying only with certain chance */
        if (Bot.rand.nextDouble() > bot.getBotSettings().getReplyChance()) {
            return;
        }

        List<ReplyRule> matchingRules = new ArrayList<>();
        ReplyRule chosenRule;

        List<ReplyRule> toRemoveRules = new ArrayList<>();
        for (ReplyRule rule : replyRules) {
            List<String> reactToList = rule.getReactToList();

            for (String singleReact : reactToList) {
                if (singleReact.length() < MIN_REPLY_TO_LENGTH) {
                    toRemoveRules.add(rule);
                    log.trace("Rule {} will be removed due to shortness.", rule);

                    continue;
                }

                if ((bot.getBotSettings().getMatchingStrategy() == Const.MatchingStrategy.inline)
                        && message.getContentStripped().contains(singleReact)) {
                    log.trace("Inline react to trigger '{}' that was found in '{}'.", singleReact, message.toString());
                    matchingRules.add(rule);
                }

                if ((bot.getBotSettings().getMatchingStrategy() == Const.MatchingStrategy.full)
                        && message.getContentStripped().equals(singleReact)) {
                    log.trace("Full react to trigger '{}' that was found in '{}'.", singleReact, message.toString());
                    matchingRules.add(rule);
                }
            }
        }

        if (!toRemoveRules.isEmpty()) {
            if (replyRules.removeAll(toRemoveRules)) {
                try {
                    writeRules();
                    log.info("Reply rules were automatically removed due to shortness.");
                    log.trace("Removed rules: {}", Arrays.toString(toRemoveRules.toArray()));
                } catch (IOException e) {
                    log.error("Failed to remove rules that should be removed!", e);
                }
            }
        }

        if (matchingRules.isEmpty()) {
            log.trace("Matching rules are empty for message '{}'. No reply.", message.toString());

            return;
        }

        if (matchingRules.size() > 1) {
            chosenRule = matchingRules.get(Bot.rand.nextInt(matchingRules.size()));
        } else {
            chosenRule = matchingRules.get(0);
        }

        log.trace("Sending reply to '{}' with '{}'.", message.toString(), chosenRule);
        message.getTextChannel().sendMessage(
                chosenRule.getReactWithList().get(
                        Bot.rand.nextInt(chosenRule.getReactWithList().size()))
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

        FileUtils.createFolderIfAbsent(rulesFolder);

        if (replyRules.isEmpty()) {
            readRules();
        }

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
        File rulesFile = new File(rulesFolder + REPLY_RULES_JSON);

        if (!rulesFile.exists()) {
            FileUtils.createFolderIfAbsent(rulesFolder);

            return;
        }

        replyRules = Bot.gson.fromJson(new FileReader(rulesFile), new TypeToken<List<ReplyRule>>(){}.getType());
    }

    private void writeRules() throws IOException {
        FileUtils.writeGson(replyRules, new File(rulesFolder + REPLY_RULES_JSON));
    }
}
