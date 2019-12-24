package com.l1sk1sh.vladikbot.domain;

public class Quote {
    private final String _id;
    private final String content;
    private final String author;

    public Quote(String _id, String content, String author) {
        this._id = _id;
        this.content = content;
        this.author = author;
    }

    public String get_id() {
        return _id;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }
}
