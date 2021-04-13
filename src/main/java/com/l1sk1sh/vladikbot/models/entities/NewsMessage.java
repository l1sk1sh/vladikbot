package com.l1sk1sh.vladikbot.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class NewsMessage {
    private String title;
    private String description;
    private String imageUrl;
    private String articleUrl;
    private Date publicationDate;
    private String resourceImageUrl;
}
