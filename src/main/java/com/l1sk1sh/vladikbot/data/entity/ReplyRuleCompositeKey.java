package com.l1sk1sh.vladikbot.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author l1sk1sh
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class ReplyRuleCompositeKey implements Serializable {

    @Column(name = "trigger_id")
    long triggerId;

    @Column(name = "reaction_id")
    long reactionId;
}
