package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.DogFact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Oliver Johnson
 */
@Service
public class DogFactCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(DogFactCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public DogFactCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "dogfact";
        this.help = "get random dog fact";
    }

    @Override
    protected void execute(CommandEvent event) {
        DogFact dogFact;
        try {
            dogFact = restTemplate.getForObject("https://dog-api.kinduff.com/api/facts?number=1", DogFact.class);
        } catch (RestClientException e) {
            event.replyError(String.format("Error occurred: `%1$s`", e.getLocalizedMessage()));
            log.error("Failed to consume API.", e);

            return;
        }

        if (dogFact == null) {
            log.error("Response body is empty.");
            event.replyWarning("Couldn't get dog fact.");

            return;
        }

        event.reply(dogFact.getFact());
    }
}
