package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.network.dto.CatFact;
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
public class CatFactCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(CatFactCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
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
            event.replyFormat("Error occurred: `%1$s`", e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (catFact == null) {
            event.reply("Failed to retrieve cat's fact.").setEphemeral(true).queue();
            log.error("Failed to retrieve a cat fact.");

            return;
        }

        event.reply(catFact.getFact()).queue();
    }
}
