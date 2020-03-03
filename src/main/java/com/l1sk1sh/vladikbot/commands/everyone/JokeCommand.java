package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.DadJoke;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class JokeCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(JokeCommand.class);

    public JokeCommand() {
        this.name = "joke";
        this.help = "get random dad joke";
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Request request = new Request.Builder()
                    .addHeader("Accept", "application/json")
                    .url("https://icanhazdadjoke.com/")
                    .build();

            Response response = Bot.httpClient.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("Response body is empty.");
                event.replyWarning("Couldn't get any jokes");

                return;
            }

            DadJoke dadJoke = Bot.gson.fromJson(body.string(), DadJoke.class);

            event.reply(String.format("\"%1$s\"", dadJoke.getJoke()));
        } catch (IOException e) {
            log.error("Failed to retrieve a dad joke.", e);
            event.replyError("Failed to retrieve a dad joke.");
        }
    }
}
