package com.l1sk1sh.vladikbot.data.entity;

import com.l1sk1sh.vladikbot.settings.Const;
import lombok.*;

import javax.persistence.*;

/**
 * @author l1sk1sh
 */
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "activity_rules")
public class Activity {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "activity_name", nullable = false)
    @NonNull
    private String activityName;

    @Column(name = "status_action", nullable = false)
    @NonNull
    private Const.StatusAction statusAction;
}
