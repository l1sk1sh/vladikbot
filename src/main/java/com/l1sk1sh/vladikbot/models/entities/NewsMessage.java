package com.l1sk1sh.vladikbot.models.entities;

import java.util.Date;

public class NewsMessage {
    private String title;
    private String description;
    private String imageUrl;
    private String articleUrl;
    private Date publicationDate;
    private String resourceImageUrl;

    public NewsMessage(String title, String description, String imageUrl, String articleUrl, Date publicationDate,  String resourceImageUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.articleUrl = articleUrl;
        this.publicationDate = publicationDate;
        this.resourceImageUrl = resourceImageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getArticleUrl() {
        return articleUrl;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public String getResourceImageUrl() {
        return resourceImageUrl;
    }
}
