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
@Table(name = "reply_reactions")
public class ReplyReaction {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "reaction", nullable = false, length = Message.MAX_CONTENT_LENGTH)
    @NonNull
    private String reaction;

    @OneToMany(mappedBy = "reaction")
    Set<ReplyRule> triggeredWith;
}
