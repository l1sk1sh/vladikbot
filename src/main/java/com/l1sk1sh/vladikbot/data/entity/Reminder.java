package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

/**
 * @author l1sk1sh
 */
@SuppressWarnings("unused")
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "reminders")
public class Reminder {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "date", nullable = false)
    @NonNull
    private Date dateOfReminder;

    @Column(name = "text", nullable = false)
    @NonNull
    private String textOfReminder;

    @Column(name = "text_channel_id")
    @NonNull
    private Long textChannelId;

    @Column(name = "author_id")
    @NonNull
    private Long authorId;
}

