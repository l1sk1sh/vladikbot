package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.DadJoke;
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
public class JokeCommand extends SlashCommand {

    private final RestTemplate restTemplate;

    public JokeCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "joke";
        this.help = "Get a random dad joke";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        DadJoke dadJoke;
        try {
            dadJoke = restTemplate.getForObject("https://icanhazdadjoke.com/", DadJoke.class);
        } catch (RestClientException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (dadJoke == null) {
            event.replyFormat("%1$s Couldn't get any jokes", event.getClient().getError()).setEphemeral(true).queue();
            log.error("Response body is empty.");

            return;
        }

        event.replyFormat("\"%1$s\"", dadJoke.getJoke()).queue();
    }
}
