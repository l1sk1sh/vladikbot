package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class QuoteCommand extends SlashCommand {

    private static final String DEFAULT_QUOTE = "\"Я тебе породив, я тебе і вб'ю!\" Тарас Бульба";

    private final RestTemplate restTemplate;

    public QuoteCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "quote";
        this.help = "Get a random quote";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Quote quote;
        try {
            quote = restTemplate.getForObject("https://api.quotable.io/random", Quote.class);
        } catch (RestClientException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (quote == null) {
            log.warn("Quote is empty.");
            event.reply(DEFAULT_QUOTE).setEphemeral(true).queue();

            return;
        }

        event.replyFormat("\"%1$s\" %2$s", quote.getContent(), quote.getAuthor()).queue();
    }
}
