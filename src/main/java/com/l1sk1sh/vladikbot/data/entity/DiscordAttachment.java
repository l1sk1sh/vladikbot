package com.l1sk1sh.vladikbot.data.entity;

import lombok.*;
import net.dv8tion.jda.api.entities.Message;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "discord_attachments")
public class DiscordAttachment {

    @Id
    private long id;

    @Column(name = "discord_message_id")
    private long discordMessageId;

    @Column(name = "url", nullable = false, length = Message.MAX_CONTENT_LENGTH)
    @NonNull
    private String url;

    @Column(name = "file_name", nullable = false)
    @NonNull
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "downloaded")
    private boolean downloaded = false;

    @Column(name = "download_failed")
    private boolean downloadFailed = false;
}
