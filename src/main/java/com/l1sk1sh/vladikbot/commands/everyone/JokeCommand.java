package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.network.dto.DadJoke;
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
public class JokeCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(JokeCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
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
            event.replyFormat("Error occurred: `%1$s`", e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (dadJoke == null) {
            log.error("Response body is empty.");
            event.reply("Couldn't get any jokes").setEphemeral(true).queue();

            return;
        }

        event.reply(String.format("\"%1$s\"", dadJoke.getJoke())).queue();
    }
}
