package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.CatPicture;
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
public class CatPictureCommand extends SlashCommand {

    private final RestTemplate restTemplate;

    @Autowired
    public CatPictureCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "cat";
        this.help = "Get a random cat picture";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        CatPicture[] catPictures;
        try {
            catPictures = restTemplate.getForObject("https://api.thecatapi.com/v1/images/search", CatPicture[].class);
        } catch (RestClientException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (catPictures == null) {
            event.replyFormat("%1$s Failed to retrieve cat's picture.", event.getClient().getError()).setEphemeral(true).queue();
            log.error("Failed to retrieve a cat picture.");

            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Meow!", null, null)
                .setColor(new Color(20, 120, 120))
                .setImage(catPictures[0].getUrl());

        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
