package com.l1sk1sh.vladikbot.domain;

public class Meme {
    private String postLink;
    private String subreddit;
    private String title;
    private String url;
    private boolean nsfw;
    private boolean spoiler;

    public String getPostLink() {
        return postLink;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    public boolean isSpoiler() {
        return spoiler;
    }
}
