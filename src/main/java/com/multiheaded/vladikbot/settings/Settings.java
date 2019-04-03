package com.multiheaded.vladikbot.settings;

import com.multiheaded.vladikbot.utils.FormatUtil;
import com.multiheaded.vladikbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class Settings {
    private String token = "MY_BOT_TOKEN";
    private Long ownerId = 0L;
    private String dockerContainerName = "disbackup";
    private String dockerPathToExport = "/a/";
    private String localPathToExport = "/var/tmp/";
    private String prefix = "!";
    private String helpWord = "help";
    private String successEmoji = "\uD83D\uDC4C"; //👌
    private String warningEmoji = "\uD83D\uDD95"; //🖕
    private String errorEmoji = "\uD83D\uDCA2"; //💢
    private String loadingEmoji = "\uD83E\uDDF6"; //🧶
    private String searchingEmoji = "\uD83D\uDD0E"; //🔎
    private String game = "watching Ubisoft conference";
    private String onlineStatus = "ONLINE";
    private boolean leaveChannel = true;
    private boolean songInGame = false;
    private boolean npImages = true;
    private Long maxSeconds = 0L;
    private String playlistsFolder = "playlists/";
    private Long textChannelId = 0L;
    private Long voiceChannelId = 0L;
    private Long djRoleId = 0L;
    private int volume = 50;
    private String defaultPlaylist = "default_playlist";
    private boolean repeat = true;
    private boolean lockOnBackup = false;

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
        return OtherUtil.parseGame(game);
    }

    public OnlineStatus getOnlineStatus() {
        return OtherUtil.parseStatus(onlineStatus);
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

    public String getMaxTime() {
        return FormatUtil.formatTime(maxSeconds * 1000);
    }

    public boolean isTooLong(AudioTrack track) {
        return (maxSeconds > 0) && (Math.round(track.getDuration() / 1000.0) > maxSeconds);
    }

    public boolean isLockedOnBackup() {
        return lockOnBackup;
    }

    public void setLockOnBackup(boolean lockOnBackup) {
        this.lockOnBackup = lockOnBackup;
    }
}