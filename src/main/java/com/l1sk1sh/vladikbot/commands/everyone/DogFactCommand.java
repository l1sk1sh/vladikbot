package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.network.dto.DogFact;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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
public class DogFactCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(DogFactCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public DogFactCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "dogfact";
        this.help = "Get a random dog fact";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        DogFact dogFact;
        try {
            dogFact = restTemplate.getForObject("https://dog-api.kinduff.com/api/facts?number=1", DogFact.class);
        } catch (RestClientException e) {
            event.replyFormat("Error occurred: `%1$s`", e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (dogFact == null) {
            event.reply("Couldn't get dog fact.").setEphemeral(true).queue();
            log.error("Response body is empty.");

            return;
        }

        event.reply(dogFact.getFact()).queue();
    }
}
