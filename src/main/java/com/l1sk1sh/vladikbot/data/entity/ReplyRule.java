package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;
import net.dv8tion.jda.api.entities.Message;

import javax.persistence.*;
import java.util.List;

/**
 * @author l1sk1sh
 */
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "reply_rules")
public class ReplyRule {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "trigger", nullable = false, length = Message.MAX_CONTENT_LENGTH)
    @ElementCollection
    @CollectionTable(name = "reply_triggers", joinColumns = @JoinColumn(name = "id"))
    @NonNull
    private List<String> reactToList;

    @Column(name = "reaction", nullable = false, length = Message.MAX_CONTENT_LENGTH)
    @ElementCollection
    @CollectionTable(name = "reply_reactions", joinColumns = @JoinColumn(name = "id"))
    @NonNull
    private List<String> reactWithList;
}
