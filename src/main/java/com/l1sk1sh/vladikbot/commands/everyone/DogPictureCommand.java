package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.DogPicture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class DogPictureCommand extends SlashCommand {

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
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (dogPicture == null) {
            event.replyFormat("%1$s Couldn't get dog picture", event.getClient().getError()).setEphemeral(true).queue();
            log.error("Response body is empty.");

            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(20, 120, 120))
                .setImage(dogPicture.getPicture());

        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
