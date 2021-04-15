package com.l1sk1sh.vladikbot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author l1sk1sh
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class NewsDiscordMessage {
    private String title;
    private String description;
    private String imageUrl;
    private String articleUrl;
    private Date publicationDate;
    private String resourceImageUrl;
}
