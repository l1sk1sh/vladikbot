package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class QuoteCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(QuoteCommand.class);
    private final Bot bot;

    public QuoteCommand(Bot bot) {
        this.name = "quote";
        this.help = "get random quote from api.quotable.io";
        this.bot = bot;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Quote quote = bot.getRandomQuoteRetriever().call();

            if (quote == null) {
                log.warn("Quote is empty.");

                return;
            }

            event.reply(String.format("\"%1$s\" %2$s",
                    quote.getContent(),
                    quote.getAuthor()));
        } catch (IOException e) {
            log.error("Failed to retrieve random quote:", e);
            event.reply("\"Я тебе породив, я тебе і вб'ю!\" Тарас Бульба");
        }
    }
}
