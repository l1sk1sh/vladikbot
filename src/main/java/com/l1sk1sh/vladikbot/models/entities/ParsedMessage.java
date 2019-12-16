package com.l1sk1sh.vladikbot.models.entities;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.util.Date;

@SuppressWarnings("unused")
public class ParsedMessage {
    @CsvBindByName(required = true)
    private long authorID;

    @CsvBindByName(required = true)
    private String author;

    @CsvDate("dd-MMM-yy HH:m a")
    @CsvBindByName(required = true)
    private Date date;

    @CsvBindByName
    private String content;

    @CsvBindByName
    private String attachments;

    @CsvBindByName
    private String reactions;

    public ParsedMessage() {
    }

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

    public void setAuthorID(long authorID) {
        this.authorID = authorID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getReactions() {
        return reactions;
    }

    public void setReactions(String reactions) {
        this.reactions = reactions;
    }

    @Override
    public String toString() {
        return "ParsedMessage{" +
                "authorID=" + authorID +
                ", author='" + author + '\'' +
                ", date=" + date +
                ", content='" + content + '\'' +
                ", attachments='" + attachments + '\'' +
                ", reactions='" + reactions + '\'' +
                '}';
    }
}
