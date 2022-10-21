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
@Table(name = "discord_reactions")
public class DiscordReaction {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "discord_message_id")
    @NonNull
    private Long discordMessageId;

    @Column(name = "emoji_id", nullable = false)
    @NonNull
    private Long emojiId;

    @Column(name = "emoji_name", nullable = false)
    @NonNull
    private String name;
}
