package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;

import javax.persistence.*;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "discord_emotes")
public class DiscordEmote {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "discord_message_id")
    @NonNull
    private Long discordMessageId;

    @Column(name = "emote_id", nullable = false)
    @NonNull
    private Long emoteId;

    @Column(name = "available", nullable = false)
    @NonNull
    private Boolean available;

    @Column(name = "name", nullable = false)
    @NonNull
    private String name;

    @Column(name = "mention", nullable = false)
    @NonNull
    private String mention;

    @Column(name = "image_url", nullable = false)
    @NonNull
    private String imageUrl;
}
