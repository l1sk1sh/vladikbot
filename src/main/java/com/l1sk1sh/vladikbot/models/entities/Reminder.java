package com.l1sk1sh.vladikbot.models.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@SuppressWarnings("unused")
@Getter
@Setter
@AllArgsConstructor
public class Reminder {
    @Setter(AccessLevel.NONE)
    private final long id = System.currentTimeMillis();
    private Date dateOfReminder;
    private String textOfReminder;
    private String textChannelId;
    private String authorId;
}
