package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.DadJoke;
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
public class JokeCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(JokeCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public JokeCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "joke";
        this.help = "get random dad joke";
    }

    @Override
    protected void execute(CommandEvent event) {
        DadJoke dadJoke;
        try {
            dadJoke = restTemplate.getForObject("https://icanhazdadjoke.com/", DadJoke.class);
        } catch (RestClientException e) {
            event.replyError(String.format("Error occurred: `%1$s`", e.getLocalizedMessage()));
            log.error("Failed to consume API.", e);

            return;
        }

        if (dadJoke == null) {
            log.error("Response body is empty.");
            event.replyWarning("Couldn't get any jokes");

            return;
        }

        event.reply(String.format("\"%1$s\"", dadJoke.getJoke()));
    }
}
