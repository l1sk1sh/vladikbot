package com.l1sk1sh.vladikbot.services.presence;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.entity.ReplyReaction;
import com.l1sk1sh.vladikbot.data.entity.ReplyRule;
import com.l1sk1sh.vladikbot.data.entity.ReplyTrigger;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.data.repository.ReplyReactionsRepository;
import com.l1sk1sh.vladikbot.data.repository.ReplyRulesRepository;
import com.l1sk1sh.vladikbot.data.repository.ReplyTriggersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author l1sk1sh
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AutoReplyManager {

    public static final int MIN_REPLY_TO_LENGTH = 3;

    private final ReplyRulesRepository replyRulesRepository;
    private final ReplyTriggersRepository replyTriggersRepository;
    private final ReplyReactionsRepository replyReactionsRepository;
    private final GuildSettingsRepository guildSettingsRepository;
    private final Random random = new Random();
    private List<ReplyRule> replyRules = new ArrayList<>();

    public void init() {
        replyRules = replyRulesRepository.findAll();
    }

    public void reply(Message message) {
        if (message.getMentions().getMembers().contains(message.getGuild().getSelfMember())) {
            if (!replyRules.isEmpty()) {
                ReplyRule randomRule = replyRules.get(random.nextInt(replyRules.size()));
                String randomReply = randomRule.getReaction().getReaction();
                message.getChannel().asTextChannel().sendMessage(randomReply).queue();
            }
            return;
        }

        Optional<GuildSettings> settings = guildSettingsRepository.findById(message.getGuild().getIdLong());
        double replyChance = settings.map(GuildSettings::getReplyChance).orElse(GuildSettings.DEFAULT_REPLY_CHANCE);

        /* Replying only with certain chance */
        if (random.nextDouble() > replyChance) {
            return;
        }

        List<ReplyRule> matchingRules = new ArrayList<>();
        ReplyRule chosenRule;

        List<ReplyRule> toRemoveRules = new ArrayList<>();
        for (ReplyRule rule : replyRules) {
            String trigger = rule.getTrigger().getTrigger();

            if (trigger.length() < MIN_REPLY_TO_LENGTH) {
                toRemoveRules.add(rule);
                log.trace("Rule {} will be removed due to shortness.", rule);

                continue;
            }

            if (message.getContentStripped().toLowerCase().contains(trigger.toLowerCase())) {
                log.trace("React to trigger '{}' that was found in '{}'.", trigger, message.toString());
                matchingRules.add(rule);
            }
        }

        if (!toRemoveRules.isEmpty() && replyRules.removeAll(toRemoveRules)) {
            replyRulesRepository.deleteAll(toRemoveRules);
            log.info("Reply rules were automatically removed due to shortness.");
            log.trace("Removed rules: {}", Arrays.toString(toRemoveRules.toArray()));
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
        message.getChannel().asTextChannel().sendMessage(
                chosenRule.getReaction().getReaction()
        ).queue();
    }

    public void writeRule(ReplyRule rule) {
        log.debug("Writing new reply rule '{}'.", rule);

        ReplyReaction savedReaction;
        savedReaction = replyReactionsRepository.findByReaction(rule.getReaction().getReaction());
        if (savedReaction == null) {
            savedReaction = replyReactionsRepository.save(rule.getReaction());
        }

        ReplyTrigger savedTrigger;
        savedTrigger = replyTriggersRepository.findByTrigger(rule.getTrigger().getTrigger());
        if (savedTrigger == null) {
            savedTrigger = replyTriggersRepository.save(rule.getTrigger());
        }

        ReplyRule savedRule = null;

        /* In case both reactions already exist check if same rule already exist */
        if (savedReaction.getId() > 0L && savedTrigger.getId() > 0L) {
            savedRule = replyRulesRepository.getByReactionAndTrigger(savedReaction, savedTrigger);
        }

        if (savedRule != null) {
            log.warn("Rule {}:{} already exists", rule.getReaction().getId(), rule.getTrigger().getId());
            return;
        }

        savedRule = replyRulesRepository.save(new ReplyRule(savedTrigger, savedReaction));
        replyRules.add(savedRule);
    }

    public void filterRules() {
        // TODO
    }

    public void deleteRule(ReplyRule rule) {
        log.info("Trying to remove reply rule '{}'...", rule);
        replyRulesRepository.delete(rule);
        replyRules.stream().filter(r -> r.getId() == rule.getId()).findFirst()
                .ifPresent(runtimeActivity -> replyRules.remove(runtimeActivity));
    }
}
