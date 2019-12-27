package com.l1sk1sh.vladikbot.domain;

public class Quote {
    private final String content;
    private final String author;

    public Quote(String content, String author) {
        this.content = content;
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }
}
