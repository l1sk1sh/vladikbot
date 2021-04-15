package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

/**
 * @author Oliver Johnson
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

    @Column(name = "trigger", nullable = false)
    @ElementCollection
    @CollectionTable(name = "reply_trigger", joinColumns = @JoinColumn(name = "id"))
    @NonNull
    private List<String> reactToList;

    @Column(name = "reaction", nullable = false)
    @ElementCollection
    @CollectionTable(name = "reply_reaction", joinColumns = @JoinColumn(name = "id"))
    @NonNull
    private List<String> reactWithList;
}
