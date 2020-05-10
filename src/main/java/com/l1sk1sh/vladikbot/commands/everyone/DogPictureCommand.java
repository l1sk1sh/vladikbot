package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.DogPicture;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class DogPictureCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(DogPictureCommand.class);

    public DogPictureCommand() {
        this.name = "dog";
        this.help = "get random dog picture";
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Request request = new Request.Builder()
                    .url("https://dog.ceo/api/breeds/image/random")
                    .build();

            Response response = Bot.httpClient.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("Response body is empty.");
                event.replyWarning("Couldn't get dog picture");

                return;
            }

            DogPicture dogPicture = Bot.gson.fromJson(body.string(), DogPicture.class);

            MessageBuilder builder = new MessageBuilder();
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("Woof!", null, null)
                    .setColor(new Color(20, 120, 120))
                    .setImage(dogPicture.getPicture());

            event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
        } catch (IOException e) {
            log.error("Failed to retrieve a dog picture.", e);
            event.replyError("Failed to retrieve dog's picture.");
        }
    }
}
