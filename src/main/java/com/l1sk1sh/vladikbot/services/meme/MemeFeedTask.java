package com.l1sk1sh.vladikbot.services.meme;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.Meme;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;

public class MemeFeedTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MemeFeedTask.class);

    private Bot bot;

    MemeFeedTask(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        log.debug("Running memes lookup...");

        try {
            Request request = new Request.Builder()
                    .url("http://meme-api.herokuapp.com/gimme")
                    .build();

            Response response = Bot.httpClient.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("Response body is empty.");

                return;
            }

            Meme meme = Bot.gson.fromJson(body.string(), Meme.class);

            if (bot.getOfflineStorage().getLastMemeIds().contains(meme.getPostLink())) {
                return;
            }

            bot.getOfflineStorage().addSentMemeId(meme.getPostLink());

            log.info("Sending '{}' article ({}).", meme.getTitle(), meme.getPostLink());

            bot.getMemeNotificationService().sendNewsArticle(null, meme);
        } catch (UnknownHostException e) {
            log.warn("Failed to retrieve a meme due to network issues.");
        } catch (IOException e) {
            log.error("Failed to retrieve a meme due to unexpected issue.", e);
        }
    }
}
