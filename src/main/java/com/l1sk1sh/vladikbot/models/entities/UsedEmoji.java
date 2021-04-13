package com.l1sk1sh.vladikbot.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@SuppressWarnings("unused")
@AllArgsConstructor
@Getter
@Setter
public class UsedEmoji {
    private final long authorId;
    private final String author;
    private final Date date;
    private final String emoji;
    private final boolean reaction;
    private boolean unicode;
}
