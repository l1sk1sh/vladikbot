package com.l1sk1sh.vladikbot.models.entities;

import java.util.Date;

public class ParsedMessage {
    private long authorID;
    private String author;
    private Date date;
    private String content;
    private String attachments;
    private String reactions;

    public ParsedMessage(long authorID, String author, Date date, String content, String attachments, String reactions) {
        this.authorID = authorID;
        this.author = author;
        this.date = date;
        this.content = content;
        this.attachments = attachments;
        this.reactions = reactions;
    }

    public long getAuthorID() {
        return authorID;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public String getAttachments() {
        return attachments;
    }

    public String getReactions() {
        return reactions;
    }
}
