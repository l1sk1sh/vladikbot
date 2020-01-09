package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.CatFact;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CatFactCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(CatFactCommand.class);
    private final OkHttpClient client;

    public CatFactCommand() {
        this.name = "catfact";
        this.help = "get random cat fact";
        this.client = new OkHttpClient();
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Request request = new Request.Builder()
                    .url("https://catfact.ninja/fact")
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("Response body is empty.");
                event.replyWarning("Couldn't get cat fact");

                return;
            }

            CatFact catFact = Bot.gson.fromJson(body.string(), CatFact.class);

            event.reply(catFact.getFact());
        } catch (IOException e) {
            log.error("Failed to retrieve a cat fact.", e);
            event.replyError("Failed to retrieve cat's fact.");
        }
    }
}
