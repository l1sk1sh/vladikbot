package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

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
public class ReplyRule implements Serializable {

    @EmbeddedId
    ReplyRuleCompositeKey id = new ReplyRuleCompositeKey();

    @ManyToOne
    @MapsId("triggerId")
    @JoinColumn(name = "trigger_id")
    @NonNull
    ReplyTrigger trigger;

    @ManyToOne
    @MapsId("reactionId")
    @JoinColumn(name = "reaction_id")
    @NonNull
    ReplyReaction reaction;
}
