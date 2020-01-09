package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.CatPicture;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import okhttp3.OkHttpClient;
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
public class CatPictureCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(CatPictureCommand.class);
    private final OkHttpClient client;

    public CatPictureCommand() {
        this.name = "cat";
        this.help = "get random cat picture";
        this.client = new OkHttpClient();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Request request = new Request.Builder()
                    .url("https://api.thecatapi.com/v1/images/search")
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("Response body is empty.");
                event.replyWarning("Couldn't get cat picture");

                return;
            }

            CatPicture catPicture = Bot.gson.fromJson(body.string(), CatPicture[].class)[0];

            MessageBuilder builder = new MessageBuilder();
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("Meow!", null, null)
                    .setColor(new Color(20, 120, 120))
                    .setImage(catPicture.getUrl());

            event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
        } catch (IOException e) {
            log.error("Failed to retrieve a cat picture.", e);
            event.replyError("Failed to retrieve cat's picture.");
        }
    }
}
