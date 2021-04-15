package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.CatPicture;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

/**
 * @author Oliver Johnson
 */
@Service
public class CatPictureCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(CatPictureCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public CatPictureCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "cat";
        this.help = "get random cat picture";
    }

    @Override
    protected void execute(CommandEvent event) {
        CatPicture[] catPictures;
        try {
            catPictures = restTemplate.getForObject("https://api.thecatapi.com/v1/images/search", CatPicture[].class);
        } catch (RestClientException e) {
            event.replyError(String.format("Error occurred: `%1$s`", e.getLocalizedMessage()));
            log.error("Failed to consume API.", e);

            return;
        }

        if (catPictures == null) {
            log.error("Failed to retrieve a cat picture.");
            event.replyError("Failed to retrieve cat's picture.");

            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Meow!", null, null)
                .setColor(new Color(20, 120, 120))
                .setImage(catPictures[0].getUrl());

        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}
