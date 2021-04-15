package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.CatFact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author l1sk1sh
 */
@Service
public class CatFactCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(CatFactCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public CatFactCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "catfact";
        this.help = "get random cat fact";
    }

    @Override
    protected void execute(CommandEvent event) {
        CatFact catFact;
        try {
            catFact = restTemplate.getForObject("https://catfact.ninja/fact", CatFact.class);
        } catch (RestClientException e) {
            event.replyError(String.format("Error occurred: `%1$s`", e.getLocalizedMessage()));
            log.error("Failed to consume API.", e);

            return;
        }

        if (catFact == null) {
            log.error("Failed to retrieve a cat fact.");
            event.replyError("Failed to retrieve cat's fact.");

            return;
        }

        event.reply(catFact.getFact());
    }
}
