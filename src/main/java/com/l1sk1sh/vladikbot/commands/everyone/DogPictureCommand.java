package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.network.dto.DogPicture;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

/**
 * @author l1sk1sh
 */
@Service
public class DogPictureCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(DogPictureCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public DogPictureCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "dog";
        this.help = "Get a random dog picture";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        DogPicture dogPicture;
        try {
            dogPicture = restTemplate.getForObject("https://dog.ceo/api/breeds/image/random", DogPicture.class);
        } catch (RestClientException e) {
            event.replyFormat("Error occurred: `%1$s`", e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (dogPicture == null) {
            event.reply("Couldn't get dog picture").setEphemeral(true).queue();
            log.error("Response body is empty.");

            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Woof!", null, null)
                .setColor(new Color(20, 120, 120))
                .setImage(dogPicture.getPicture());

        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
