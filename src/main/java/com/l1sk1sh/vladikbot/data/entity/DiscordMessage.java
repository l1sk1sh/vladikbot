package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;
import net.dv8tion.jda.api.entities.Message;

import javax.persistence.*;
import java.util.List;

/**
 * @author l1sk1sh
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "discord_messages")
public class DiscordMessage {

    @Id
    private long id;

    @Column(name = "channel_id", nullable = false)
    private long channelId;

    @Column(name = "author_id", nullable = false)
    private long authorId;

    @Column(name = "content", length = Message.MAX_CONTENT_LENGTH)
    private String content;

    @Column(name = "created_time", nullable = false)
    private long createdTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "discord_message_id")
    private List<DiscordEmote> emotes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "discord_message_id")
    private List<DiscordReaction> reactions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "discord_message_id")
    private List<DiscordAttachment> attachments;
}
