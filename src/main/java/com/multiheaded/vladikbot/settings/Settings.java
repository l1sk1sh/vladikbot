package com.multiheaded.vladikbot.settings;

import com.multiheaded.vladikbot.utils.FormatUtils;
import com.multiheaded.vladikbot.utils.OtherUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class Settings {

    /* Finish all paths with file system separator! */
    private String token = "MY_BOT_TOKEN";                      // Bot token taken from discord developer portal
    private Long ownerId = 0L;                                  // Id of the person, who is hosting the bot
    private String dockerContainerName = "disbackup";           // Docker container name
    private String dockerPathToExport = "/app/out/";            // Docker disbackup workdir (check repository if fails)
    private String localPathToExport = "app/";                  // Local workdir
    private String localMediaFolder = "app/saved_media/";       // Local storage for downloaded media
    private String moderationRulesFolder = "app/mod_rules/";    // Local storage for automoderation settings
    private String prefix = "~";                                // Bot prefix
    private String helpWord = "help";                           // Help word used for help command
    private String successEmoji = "\uD83D\uDC4C";               // 👌
    private String warningEmoji = "\uD83D\uDD95";               // 🖕
    private String errorEmoji = "\uD83D\uDCA2";                 // 💢
    private String loadingEmoji = "\uD83E\uDDF6";               // 🧶
    private String searchingEmoji = "\uD83D\uDD0E";             // 🔎
    private String game = "watching Ubisoft conference";        // Current name of the 'game' being played by the bot
    private String onlineStatus = "ONLINE";                     // "online", "idle", "dnd", "invisible", "offline", ""
    private boolean leaveChannel = true;                        // Leave channel if no one is listening
    private boolean songInGame = false;                         // Show song as status
    private boolean npImages = true;                            // Display search images
    private Long maxSeconds = 0L;                               // Maximum song length
    private String playlistsFolder = "app/playlists/";          // Local folder for playlists to be stored
    private Long textChannelId = 0L;                            // Only one channel id for bot's texting
    private Long voiceChannelId = 0L;                           // Only one voice id for bot's music
    private Long djRoleId = 0L;                                 // Sets who can use DJ commands
    private int volume = 50;                                    // Sets volume of the bot
    private String defaultPlaylist = "default_playlist";        // Sets name of default playlist
    private boolean repeat = true;                              // If repeat mode is available
    private boolean autoModeration = false;                     // If to use moderation feature

    public String getToken() {
        return token;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getDockerContainerName() {
        return dockerContainerName;
    }

    public String getDockerPathToExport() {
        return dockerPathToExport;
    }

    public String getLocalPathToExport() {
        return localPathToExport;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getHelpWord() {
        return helpWord;
    }

    public String getSuccessEmoji() {
        return successEmoji;
    }

    public String getWarningEmoji() {
        return warningEmoji;
    }

    public String getErrorEmoji() {
        return errorEmoji;
    }

    public String getLoadingEmoji() {
        return loadingEmoji;
    }

    public String getSearchingEmoji() {
        return searchingEmoji;
    }

    public Game getGame() {
        return OtherUtils.parseGame(game);
    }

    public OnlineStatus getOnlineStatus() {
        return OtherUtils.parseStatus(onlineStatus);
    }

    public boolean shouldLeaveChannel() {
        return leaveChannel;
    }

    public boolean shouldSongBeInStatus() {
        return songInGame;
    }

    public boolean useNpImages() {
        return npImages;
    }

    public Long getMaxSeconds() {
        return maxSeconds;
    }

    public String getPlaylistsFolder() {
        return playlistsFolder;
    }

    public TextChannel getTextChannel(Guild guild) {
        return (guild == null) ? null : guild.getTextChannelById(textChannelId);
    }

    public void setTextChannelId(TextChannel textChannel) {
        this.textChannelId = textChannel == null ? 0 : textChannel.getIdLong();
        SettingsManager.getInstance().writeSettings();
    }

    public VoiceChannel getVoiceChannel(Guild guild) {
        return (guild == null) ? null : guild.getVoiceChannelById(voiceChannelId);
    }

    public void setVoiceChannelId(VoiceChannel voiceChannel) {
        this.voiceChannelId = voiceChannel == null ? 0 : voiceChannel.getIdLong();
        SettingsManager.getInstance().writeSettings();
    }

    public Role getDjRole(Guild guild) {
        return (guild == null) ? null : guild.getRoleById(djRoleId);
    }

    public void setDjRoleId(Role role) {
        this.djRoleId = role == null ? 0 : role.getIdLong();
        SettingsManager.getInstance().writeSettings();
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
        SettingsManager.getInstance().writeSettings();
    }

    public String getDefaultPlaylist() {
        return defaultPlaylist;
    }

    public void setDefaultPlaylist(String defaultPlaylist) {
        this.defaultPlaylist = defaultPlaylist;
        SettingsManager.getInstance().writeSettings();
    }

    public boolean shouldRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        SettingsManager.getInstance().writeSettings();
    }

    public boolean isAutoModeration() {
        return autoModeration;
    }

    public void setAutoModeration(boolean autoModeration) {
        this.autoModeration = autoModeration;
        SettingsManager.getInstance().writeSettings();
    }

    public String getLocalMediaFolder() {
        return localMediaFolder;
    }

    public String getModerationRulesFolder() {
        return moderationRulesFolder;
    }

    public String getMaxTime() {
        return FormatUtils.formatTime(maxSeconds * 1000);
    }

    public boolean isTooLong(AudioTrack track) {
        return (maxSeconds > 0) && (Math.round(track.getDuration() / 1000.0) > maxSeconds);
    }
}