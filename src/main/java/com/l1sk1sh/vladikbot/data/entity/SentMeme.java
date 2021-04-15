package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;

import javax.persistence.*;

/**
 * @author Oliver Johnson
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "sent_memes")
public class SentMeme {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "meme_id", nullable = false)
    @NonNull
    private String memeId;
}
