package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.DogFact;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class DogFactCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(DogFactCommand.class);
    private final OkHttpClient client;

    public DogFactCommand() {
        this.name = "dogfact";
        this.help = "get random dog fact";
        this.client = new OkHttpClient();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Request request = new Request.Builder()
                    .url("https://dog-api.kinduff.com/api/facts?number=1")
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("Response body is empty.");
                event.replyWarning("Couldn't get dog fact");

                return;
            }

            DogFact dogFact = Bot.gson.fromJson(body.string(), DogFact.class);

            event.reply(dogFact.getFact());
        } catch (IOException e) {
            log.error("Failed to retrieve a dog fact.", e);
            event.replyError("Failed to retrieve dog's fact.");
        }
    }
}
