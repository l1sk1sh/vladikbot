package com.l1sk1sh.vladikbot.services;

import com.google.gson.Gson;
import com.l1sk1sh.vladikbot.domain.Quote;
import okhttp3.OkHttpClient;
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
    private final OkHttpClient client;
    private final Gson gson;

    public RandomQuoteRetriever() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    @Override
    public Quote call() throws IOException {
        Request request = new Request.Builder()
                .url("https://api.quotable.io/random")
                .build();

        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();

        if (Objects.requireNonNull(body).contentLength() < 0) {
            return null;
        }

        return gson.fromJson(body.string(), Quote.class);
    }
}
