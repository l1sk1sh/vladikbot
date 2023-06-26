package com.l1sk1sh.vladikbot.data.entity;

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
@Table(name = "dicks")
public class Dick {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "author_id", nullable = false)
    @NonNull
    private Long authorId;

    @Column(name = "guild_id", nullable = false)
    @NonNull
    private Long guildId;

    @Column(name = "size", nullable = false)
    @NonNull
    private Integer size;

    @Column(name = "modified_time", nullable = false)
    @NonNull
    private Long modifiedTime;
}
