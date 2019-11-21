package com.l1sk1sh.vladikbot.settings;

import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.l1sk1sh.vladikbot.utils.OtherUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;

/**
 * @author Oliver Johnson
 */
@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
public class BotSettings extends AbstractSettings {
    private transient BotSettingsManager manager;

    /* Finish all paths with file system separator! */
    private String token = "MY_BOT_TOKEN";                      // Bot token taken from discord developer portal
    private Long ownerId = 0L;                                  // Id of the person, who is hosting the bot
    private String dockerContainerName = "disbackup";           // Docker container name
    private String dockerPathToExport = "/app/out/";            // Docker disbackup workdir (check repository if fails)
    private String localPathToExport = "app/tmp/";              // Local workdir
    private String localMediaFolder = "app/saved_media/";       // Local storage for downloaded media
    private String moderationRulesFolder = "app/rules/";        // Local storage for automoderation settings
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
    private boolean repeat = true;                              // If repeat mode is available
    private boolean autoModeration = false;                     // If to use moderation feature
    private boolean rotateActionsAndGames = false;              // If bot should change statuses by himself
    private String rotationListFolder = "app/rules/rotation/";  // Folder that stores statuses.json
    private boolean rotateTextBackup = true;                    // Automatically create backups of all available chats
    private boolean rotateMediaBackup = true;                   // Automatically save media from all available chats
    private Integer targetHourForTextBackup = 12;               // Set local TZ hour for text backup to be started
    private Integer targetHourForMediaBackup = 13;              // Set local TZ hour for media backup to be started
    private Integer delayDaysForTextBackup = 2;                 // Set delay in days between text backups
    private Integer delayDaysForMediaBackup = 7;                // Set delay in days between text backups

    BotSettings(BotSettingsManager manager) {
        this.manager = manager;
    }

    void setManager(BotSettingsManager manager) {
        this.manager = manager;
    }

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

    public boolean shouldRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
        manager.writeSettings();
    }

    public boolean isAutoModeration() {
        return autoModeration;
    }

    public void setAutoModeration(boolean autoModeration) {
        this.autoModeration = autoModeration;
        manager.writeSettings();
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

    public String getRotationFolder() {
        return rotationListFolder;
    }

    public boolean shouldRotateActionsAndGames() {
        return rotateActionsAndGames;
    }

    public void setRotateActionsAndGames(boolean rotateActionAndGames) {
        this.rotateActionsAndGames = rotateActionAndGames;
        manager.writeSettings();
    }

    public boolean shouldRotateTextBackup() {
        return rotateTextBackup;
    }

    public void setRotateTextBackup(boolean rotateTextBackup) {
        this.rotateTextBackup = rotateTextBackup;
        manager.writeSettings();
    }

    public boolean shouldRotateMediaBackup() {
        return rotateMediaBackup;
    }

    public void setRotateMediaBackup(boolean rotateMediaBackup) {
        this.rotateMediaBackup = rotateMediaBackup;
        manager.writeSettings();
    }

    public Integer getTargetHourForTextBackup() {
        return targetHourForTextBackup;
    }

    public void setTargetHourForTextBackup(Integer targetHourForTextBackup) {
        this.targetHourForTextBackup = targetHourForTextBackup;
    }

    public Integer getTargetHourForMediaBackup() {
        return targetHourForMediaBackup;
    }

    public void setTargetHourForMediaBackup(Integer targetHourForMediaBackup) {
        this.targetHourForMediaBackup = targetHourForMediaBackup;
    }

    public Integer getDelayDaysForTextBackup() {
        return delayDaysForTextBackup;
    }

    public Integer getDelayDaysForMediaBackup() {
        return delayDaysForMediaBackup;
    }
}