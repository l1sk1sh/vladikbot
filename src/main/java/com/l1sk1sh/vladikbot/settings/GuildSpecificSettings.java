package com.l1sk1sh.vladikbot.settings;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;


/**
 * @author Oliver Johnson
 */
public class GuildSpecificSettings {
    private transient GuildSpecificSettingsManager manager;

    private long textChannelId = 0L;                            // Only one channel id for bot's texting
    private long voiceChannelId = 0L;                           // Only one voice id for bot's music
    private long notificationChannelId = 0L;                    // Use separate system notification channel for bot
    private long newsChannelId = 0L;                            // Use separate channel for news notifications
    private long memesChannelId = 0L;                            // Use separate channel for memes notifications
    private long djRoleId = 0L;                                 // Sets who can use DJ commands
    private String defaultPlaylist = "default_playlist";        // Sets name of default playlist
    private int volume = 50;                                    // Sets volume of the bot

    GuildSpecificSettings(GuildSpecificSettingsManager manager) {
        this.manager = manager;
    }

    final void setManager(GuildSpecificSettingsManager manager) {
        this.manager = manager;
    }

    public final TextChannel getTextChannel(Guild guild) {
        return (guild == null) ? null : guild.getTextChannelById(textChannelId);
    }

    public final void setTextChannelId(TextChannel textChannel) {
        this.textChannelId = textChannel == null ? 0 : textChannel.getIdLong();
        manager.writeSettings();
    }

    public final VoiceChannel getVoiceChannel(Guild guild) {
        return (guild == null) ? null : guild.getVoiceChannelById(voiceChannelId);
    }

    public final void setVoiceChannelId(VoiceChannel voiceChannel) {
        this.voiceChannelId = voiceChannel == null ? 0 : voiceChannel.getIdLong();
        manager.writeSettings();
    }

    public final TextChannel getNotificationChannel(Guild guild) {
        return (guild == null) ? null : guild.getTextChannelById(notificationChannelId);
    }

    public final void setNotificationChannelId(TextChannel textChannel) {
        this.notificationChannelId = textChannel == null ? 0 : textChannel.getIdLong();
        manager.writeSettings();
    }

    public final TextChannel getNewsChannel(Guild guild) {
        return (guild == null) ? null : guild.getTextChannelById(newsChannelId);
    }

    public final void setNewsChannelId(TextChannel textChannel) {
        this.newsChannelId = textChannel == null ? 0 : textChannel.getIdLong();
        manager.writeSettings();
    }

    public final TextChannel getMemesChannel(Guild guild) {
        return (guild == null) ? null : guild.getTextChannelById(memesChannelId);
    }

    public final void setMemesChannelId(TextChannel textChannel) {
        this.memesChannelId = textChannel == null ? 0 : textChannel.getIdLong();
        manager.writeSettings();
    }

    public final Role getDjRole(Guild guild) {
        return (guild == null) ? null : guild.getRoleById(djRoleId);
    }

    public final void setDjRoleId(Role role) {
        this.djRoleId = role == null ? 0 : role.getIdLong();
        manager.writeSettings();
    }

    public final String getDefaultPlaylist() {
        return defaultPlaylist;
    }

    public final void setDefaultPlaylist(String defaultPlaylist) {
        this.defaultPlaylist = defaultPlaylist;
        manager.writeSettings();
    }

    public final int getVolume() {
        return volume;
    }

    public final void setVolume(int volume) {
        this.volume = volume;
        manager.writeSettings();
    }
}
