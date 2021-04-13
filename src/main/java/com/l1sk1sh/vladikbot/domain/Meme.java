package com.l1sk1sh.vladikbot.domain;

import lombok.Getter;

@SuppressWarnings("unused")
@Getter
public class Meme {
    private String postLink;
    private String subreddit;
    private String title;
    private String url;
    private boolean nsfw;
    private boolean spoiler;
}
