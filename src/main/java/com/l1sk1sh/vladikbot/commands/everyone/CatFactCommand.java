package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.CatFact;
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
public class CatFactCommand extends SlashCommand {

    private final RestTemplate restTemplate;

    public CatFactCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "catfact";
        this.help = "Get a random cat fact";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        CatFact catFact;
        try {
            catFact = restTemplate.getForObject("https://catfact.ninja/fact", CatFact.class);
        } catch (RestClientException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (catFact == null) {
            event.replyFormat("%1$s Failed to retrieve cat's fact.", event.getClient().getError()).setEphemeral(true).queue();
            log.error("Failed to retrieve a cat fact.");

            return;
        }

        event.reply(catFact.getFact()).queue();
    }
}
