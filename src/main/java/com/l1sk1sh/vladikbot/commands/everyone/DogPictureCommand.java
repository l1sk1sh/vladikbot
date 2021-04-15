package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.DogPicture;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

/**
 * @author Oliver Johnson
 */
@Service
public class DogPictureCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(DogPictureCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public DogPictureCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "dog";
        this.help = "get random dog picture";
    }

    @Override
    protected void execute(CommandEvent event) {
        DogPicture dogPicture = restTemplate.getForObject("https://dog.ceo/api/breeds/image/random", DogPicture.class);

        if (dogPicture == null) {
            log.error("Response body is empty.");
            event.replyWarning("Couldn't get dog picture");

            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Woof!", null, null)
                .setColor(new Color(20, 120, 120))
                .setImage(dogPicture.getPicture());

        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}
