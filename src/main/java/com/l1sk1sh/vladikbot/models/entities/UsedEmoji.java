package com.l1sk1sh.vladikbot.models.entities;

import java.util.Date;

@SuppressWarnings("unused")
public class UsedEmoji {
    private long authorId;
    private String author;
    private Date date;
    private String emoji;
    private boolean reaction;
    private boolean unicode;

    public UsedEmoji(long authorId, String author, Date date, String emoji, boolean reaction, boolean unicode) {
        this.authorId = authorId;
        this.author = author;
        this.date = date;
        this.emoji = emoji;
        this.reaction = reaction;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    public String getEmoji() {
        return emoji;
    }

    public boolean isReaction() {
        return reaction;
    }

    public boolean isUnicode() {
        return unicode;
    }
}
