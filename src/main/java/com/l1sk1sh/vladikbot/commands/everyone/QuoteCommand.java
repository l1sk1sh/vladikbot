package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author Oliver Johnson
 */
@Service
public class QuoteCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(QuoteCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public QuoteCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "quote";
        this.help = "get random quote from api.quotable.io";
    }

    @Override
    protected void execute(CommandEvent event) {
        Quote quote = restTemplate.getForObject("https://api.quotable.io/random", Quote.class);

        if (quote == null) {
            log.warn("Quote is empty.");
            event.reply("\"Я тебе породив, я тебе і вб'ю!\" Тарас Бульба");

            return;
        }

        event.reply(String.format("\"%1$s\" %2$s",
                quote.getContent(),
                quote.getAuthor()));
    }
}
