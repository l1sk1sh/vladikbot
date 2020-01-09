package com.l1sk1sh.vladikbot.services.retrievers;

import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.Quote;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author Oliver Johnson
 */
public class RandomQuoteRetriever implements Callable<Quote> {

    @Override
    public Quote call() throws IOException {
        Request request = new Request.Builder()
                .url("https://api.quotable.io/random")
                .build();

        Response response = Bot.httpClient.newCall(request).execute();
        ResponseBody body = response.body();

        if (Objects.requireNonNull(body).contentLength() < 0) {
            return null;
        }

        return Bot.gson.fromJson(body.string(), Quote.class);
    }
}
