package com.l1sk1sh.vladikbot.data.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Oliver Johnson
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@DynamicUpdate
@Table(name = "guilds_settings")
public class GuildSettings {

    @Id
    @Column(name = "guild_id")
    private long guildId;                                       // Id of guild settings belong's to

    @Column(name = "text_channel_id")
    private long textChannelId = 0L;                            // Only one channel id for bot's texting

    @Column(name = "voice_channel_id")
    private long voiceChannelId = 0L;                           // Only one voice id for bot's music

    @Column(name = "notification_channel_id")
    private long notificationChannelId = 0L;                    // Use separate system notification channel for bot

    @Column(name = "news_channel_id")
    private long newsChannelId = 0L;                            // Use separate channel for news notifications

    @Column(name = "memes_channel_id")
    private long memesChannelId = 0L;                           // Use separate channel for memes notifications

    @Column(name = "dj_role_user_id")
    private long djRoleId = 0L;                                 // Sets who can use DJ commands

    @Column(name = "default_playlist")
    private String defaultPlaylist = "default_playlist";        // Sets name of default playlist

    @Column(name = "volume")
    private int volume = 50;                                    // Sets volume of the bot

    public final TextChannel getTextChannel(@NotNull Guild guild) {
        return guild.getTextChannelById(textChannelId);
    }

    public final VoiceChannel getVoiceChannel(@NotNull Guild guild) {
        return guild.getVoiceChannelById(voiceChannelId);
    }

    public final TextChannel getNotificationChannel(@NotNull Guild guild) {
        return guild.getTextChannelById(notificationChannelId);
    }

    public final TextChannel getNewsChannel(@NotNull Guild guild) {
        return guild.getTextChannelById(newsChannelId);
    }

    public final TextChannel getMemesChannel(@NotNull Guild guild) {
        return guild.getTextChannelById(memesChannelId);
    }

    public final Role getDjRole(@NotNull Guild guild) {
        return guild.getRoleById(djRoleId);
    }
}
