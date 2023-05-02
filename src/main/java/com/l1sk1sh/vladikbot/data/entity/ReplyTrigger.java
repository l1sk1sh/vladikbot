package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;
import net.dv8tion.jda.api.entities.Message;

import javax.persistence.*;
import java.util.Set;

/**
 * @author l1sk1sh
 */
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "reply_triggers")
public class ReplyTrigger {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "trigger", nullable = false, length = Message.MAX_CONTENT_LENGTH)
    @NonNull
    private String trigger;

    @OneToMany(mappedBy = "trigger")
    Set<ReplyRule> reactTo;
}
