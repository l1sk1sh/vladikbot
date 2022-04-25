package com.l1sk1sh.vladikbot.data.entity;

import com.l1sk1sh.vladikbot.services.notification.NewsNotificationService;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
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

import javax.persistence.*;

/**
 * @author l1sk1sh
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@DynamicUpdate
@Table(name = "guild_settings")
public class GuildSettings {

    public static final double DEFAULT_REPLY_CHANCE = 0.5;
    public static final AutoReplyManager.MatchingStrategy DEFAULT_MATCHING_STRATEGY = AutoReplyManager.MatchingStrategy.INLINE;
    public static final NewsNotificationService.NewsStyle DEFAULT_NEWS_STYLE = NewsNotificationService.NewsStyle.FULL;

    @Id
    @Column(name = "guild_id")
    private long guildId;                                       // Id of guild settings belong's to

    @Column(name = "text_channel_id")
    private long textChannelId = 0L;                            // Only one channel id for bot's texting

    @Column(name = "voice_channel_id")
    private long voiceChannelId = 0L;                           // Only one voice id for bot's music

    @Column(name = "auto_reply")
    private boolean autoReply = false;                          // Reply to incoming messages

    @Column(name = "auto_reply_chance")
    private double replyChance = DEFAULT_REPLY_CHANCE;          // Chance of reply

    @Enumerated(EnumType.STRING)
    @Column(name = "auto_reply_strategy")
    private AutoReplyManager.MatchingStrategy matchingStrategy  // How matching of replies is done
            = DEFAULT_MATCHING_STRATEGY;

    @Column(name = "log_guild_changes")
    private boolean logGuildChanges = false;                    // Log guild moderation events

    @Column(name = "notification_channel_id")
    private long notificationChannelId = 0L;                    // Use separate system notification channel for bot

    @Column(name = "send_news")
    private boolean sendNews = false;                           // Send news into this guild

    @Enumerated(EnumType.STRING)
    @Column(name = "news_style")
    private NewsNotificationService.NewsStyle newsStyle         // Stule of news message
            = DEFAULT_NEWS_STYLE;

    @Column(name = "news_channel_id")
    private long newsChannelId = 0L;                            // Use separate channel for news notifications

    @Column(name = "send_memes")
    private boolean sendMemes = false;                          // Send memes into this guild

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
