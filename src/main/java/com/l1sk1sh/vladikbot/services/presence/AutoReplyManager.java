package com.l1sk1sh.vladikbot.services.presence;

import com.l1sk1sh.vladikbot.data.entity.ReplyRule;
import com.l1sk1sh.vladikbot.data.repository.ReplyRulesRepository;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Oliver Johnson
 */
@Service
public class AutoReplyManager {
    private static final Logger log = LoggerFactory.getLogger(AutoReplyManager.class);

    public static final int MIN_REPLY_TO_LENGTH = 3;

    private final Random random;
    private final BotSettingsManager settings;
    private final ReplyRulesRepository replyRulesRepository;
    private List<ReplyRule> replyRules;

    @Autowired
    public AutoReplyManager(BotSettingsManager settings, ReplyRulesRepository replyRulesRepository) {
        this.settings = settings;
        this.replyRulesRepository = replyRulesRepository;
        this.replyRules = new ArrayList<>();
        this.random = new Random();
    }

    public void init() {
        replyRules = replyRulesRepository.findAll();
    }

    public void reply(Message message) {
        if (message.getMentionedMembers().contains(message.getGuild().getSelfMember())) {
            message.getTextChannel().sendMessage("\"I'm sorry, I don't take orders. I barely take suggestions.\"").queue();
            return;
        }

        /* Replying only with certain chance */
        if (random.nextDouble() > settings.get().getReplyChance()) {
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

                if ((settings.get().getMatchingStrategy() == Const.MatchingStrategy.inline)
                        && message.getContentStripped().contains(singleReact)) {
                    log.trace("Inline react to trigger '{}' that was found in '{}'.", singleReact, message.toString());
                    matchingRules.add(rule);
                }

                if ((settings.get().getMatchingStrategy() == Const.MatchingStrategy.full)
                        && message.getContentStripped().equals(singleReact)) {
                    log.trace("Full react to trigger '{}' that was found in '{}'.", singleReact, message.toString());
                    matchingRules.add(rule);
                }
            }
        }

        if (!toRemoveRules.isEmpty()) {
            if (replyRules.removeAll(toRemoveRules)) {
                replyRulesRepository.deleteAll(toRemoveRules);
                log.info("Reply rules were automatically removed due to shortness.");
                log.trace("Removed rules: {}", Arrays.toString(toRemoveRules.toArray()));
            }
        }

        if (matchingRules.isEmpty()) {
            log.trace("Matching rules are empty for message '{}'. No reply.", message.toString());

            return;
        }

        if (matchingRules.size() > 1) {
            chosenRule = matchingRules.get(random.nextInt(matchingRules.size()));
        } else {
            chosenRule = matchingRules.get(0);
        }

        log.trace("Sending reply to '{}' with '{}'.", message.toString(), chosenRule);
        message.getTextChannel().sendMessage(
                chosenRule.getReactWithList().get(
                        random.nextInt(chosenRule.getReactWithList().size()))
        ).queue();
    }

    public ReplyRule getRuleById(long id) {
        for (ReplyRule rule : replyRules) {
            if (rule.getId() == id) {
                return rule;
            }
        }

        return null;
    }

    public void writeRule(ReplyRule rule) {
        log.debug("Writing new reply rule '{}'.", rule);

        if (getRuleById(rule.getId()) != null) {
            log.info("Rule '{}' already exists. Removing...", rule.getId());
            deleteRule(rule);
        }

        replyRules.add(rule);
    }

    public void deleteRule(ReplyRule rule) {
        log.info("Trying to remove reply rule '{}'...", rule);
        if (replyRules.remove(rule)) {
            replyRulesRepository.delete(rule);
            log.info("Rule '{}' has been removed.", rule);
        }
    }

    public List<ReplyRule> getAllRules() {
        return replyRules;
    }
}
