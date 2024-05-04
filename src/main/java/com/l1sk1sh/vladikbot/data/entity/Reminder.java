package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author l1sk1sh
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Builder.Default
    @Column(name = "repeat")
    private boolean repeat = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_period")
    private RepeatPeriod repeatPeriod;

    @Column(name = "text", nullable = false)
    @NonNull
    private String textOfReminder;

    @Column(name = "text_channel_id")
    @NonNull
    private Long textChannelId;

    @Builder.Default
    @Column(name = "tag_author")
    private boolean tagAuthor = true;

    @Column(name = "author_id")
    @NonNull
    private Long authorId;

    @SuppressWarnings("CopyConstructorMissesField")
    public Reminder(Reminder copyFrom) {
        this.dateOfReminder = copyFrom.dateOfReminder;
        this.repeat = copyFrom.repeat;
        this.repeatPeriod = copyFrom.repeatPeriod;
        this.textOfReminder = copyFrom.textOfReminder;
        this.textChannelId = copyFrom.textChannelId;
        this.tagAuthor = copyFrom.tagAuthor;
        this.authorId = copyFrom.authorId;
    }

    @Getter
    @RequiredArgsConstructor
    public enum RepeatPeriod {
        DAILY(TimeUnit.DAYS.toMillis(1)),
        WEEKLY(TimeUnit.DAYS.toMillis(7)),
        MONTHLY(TimeUnit.DAYS.toMillis(30)),
        YEARLY(TimeUnit.DAYS.toMillis(365));

        private final long delay;
    }
}

