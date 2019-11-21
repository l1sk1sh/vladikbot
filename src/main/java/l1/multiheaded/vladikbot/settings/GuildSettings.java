package l1.multiheaded.vladikbot.settings;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;


/**
 * @author Oliver Johnson
 */
public class GuildSettings extends AbstractSettings {
    private transient GuildSettingsManager manager;

    private Long textChannelId = 0L;                            // Only one channel id for bot's texting
    private Long voiceChannelId = 0L;                           // Only one voice id for bot's music
    private Long notificationChannelId = 0L;                    // Use separate system notification channel for bot
    private Long djRoleId = 0L;                                 // Sets who can use DJ commands
    private String defaultPlaylist = "default_playlist";        // Sets name of default playlist
    private int volume = 50;                                    // Sets volume of the bot

    GuildSettings(GuildSettingsManager manager) {
        this.manager = manager;
    }

    public void setManager(GuildSettingsManager manager) {
        this.manager = manager;
    }

    public TextChannel getTextChannel(Guild guild) {
        return (guild == null) ? null : guild.getTextChannelById(textChannelId);
    }

    public void setTextChannelId(TextChannel textChannel) {
        this.textChannelId = textChannel == null ? 0 : textChannel.getIdLong();
        manager.writeSettings();
    }

    public VoiceChannel getVoiceChannel(Guild guild) {
        return (guild == null) ? null : guild.getVoiceChannelById(voiceChannelId);
    }

    public void setVoiceChannelId(VoiceChannel voiceChannel) {
        this.voiceChannelId = voiceChannel == null ? 0 : voiceChannel.getIdLong();
        manager.writeSettings();
    }

    public TextChannel getNotificationChannel(Guild guild) {
        return (guild == null) ? null : guild.getTextChannelById(notificationChannelId);
    }

    public void setNotificationChannelId(TextChannel textChannel) {
        this.notificationChannelId = textChannel == null ? 0 : textChannel.getIdLong();
        manager.writeSettings();
    }

    public Role getDjRole(Guild guild) {
        return (guild == null) ? null : guild.getRoleById(djRoleId);
    }

    public void setDjRoleId(Role role) {
        this.djRoleId = role == null ? 0 : role.getIdLong();
        manager.writeSettings();
    }

    public String getDefaultPlaylist() {
        return defaultPlaylist;
    }

    public void setDefaultPlaylist(String defaultPlaylist) {
        this.defaultPlaylist = defaultPlaylist;
        manager.writeSettings();
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
        manager.writeSettings();
    }
}
