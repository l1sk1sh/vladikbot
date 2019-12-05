package com.l1sk1sh.vladikbot.settings;

import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.l1sk1sh.vladikbot.utils.BotUtils;
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
    private String token = "MY_BOT_TOKEN";                              // Bot token taken from discord developer portal
    private long ownerId = 0L;                                          // Id of the person, who is hosting the bot
    private String dockerContainerName = "disbackup";                   // Docker container name
    private String localTmpFolder = "./app/tmp/";                       // Local tmp for workdir
    private String dockerPathToExport = "/app/out/";                    // Docker disbackup workdir (check repository if fails)
    private String rotationBackupFolder = "./app/backup/";              // Local rotation backup folder (that will be stored)
    private String playlistsFolder = "./app/playlists/";                // Local folder for playlists to be stored
    private String moderationRulesFolder = "./app/rules/";              // Local storage for automoderation settings
    private String rotationListFolder = "./app/rules/rotation/";        // Folder that stores statuses.json
    private String prefix = "~";                                        // Bot prefix
    private String helpWord = "help";                                   // Help word used for help command
    private String successEmoji = "\uD83D\uDC4C";                       // 👌
    private String warningEmoji = "\uD83D\uDD95";                       // 🖕
    private String errorEmoji = "\uD83D\uDCA2";                         // 💢
    private String loadingEmoji = "\uD83E\uDDF6";                       // 🧶
    private String searchingEmoji = "\uD83D\uDD0E";                     // 🔎
    private String game = "watching Ubisoft conference";                // Current name of the 'game' being played by the bot
    private String onlineStatus = "ONLINE";                             // "online", "idle", "dnd", "invisible", "offline", ""
    private long maxSeconds = 0L;                                       // Maximum song length
    private boolean leaveChannel = true;                                // Leave channel if no one is listening
    private boolean songInGame = false;                                 // Show song as status
    private boolean npImages = true;                                    // Display search images
    private boolean repeat = true;                                      // If repeat mode is available
    private boolean autoModeration = false;                             // If to use moderation feature
    private boolean rotateActionsAndGames = false;                      // If bot should change statuses by himself
    private boolean rotateTextBackup = true;                            // Automatically create backups of all available chats
    private boolean rotateMediaBackup = true;                           // Automatically save media from all available chats
    private int targetHourForTextBackup = 0;                            // Set local TZ hour for text backup to be started
    private int targetHourForMediaBackup = 0;                           // Set local TZ hour for media backup to be started
    private int delayDaysForTextBackup = 0;                             // Set delay in days between text backups
    private int delayDaysForMediaBackup = 0;                            // Set delay in days between text backups

    BotSettings(BotSettingsManager manager) {
        this.manager = manager;
    }

    final void setManager(BotSettingsManager manager) {
        this.manager = manager;
    }

    public final String getToken() {
        return token;
    }

    public final long getOwnerId() {
        return ownerId;
    }

    public final String getDockerContainerName() {
        return dockerContainerName;
    }

    public final String getDockerPathToExport() {
        return dockerPathToExport;
    }

    public final String getLocalTmpFolder() {
        return localTmpFolder;
    }

    public String getRotationBackupFolder() {
        return rotationBackupFolder;
    }

    public final String getPrefix() {
        return prefix;
    }

    public final String getHelpWord() {
        return helpWord;
    }

    public final String getSuccessEmoji() {
        return successEmoji;
    }

    public final String getWarningEmoji() {
        return warningEmoji;
    }

    public final String getErrorEmoji() {
        return errorEmoji;
    }

    public final String getLoadingEmoji() {
        return loadingEmoji;
    }

    public final String getSearchingEmoji() {
        return searchingEmoji;
    }

    public Game getGame() {
        return BotUtils.parseGame(game);
    }

    public final OnlineStatus getOnlineStatus() {
        return BotUtils.parseStatus(onlineStatus);
    }

    public final boolean shouldLeaveChannel() {
        return leaveChannel;
    }

    public final boolean shouldSongBeInStatus() {
        return songInGame;
    }

    public final boolean useNpImages() {
        return npImages;
    }

    public final long getMaxSeconds() {
        return maxSeconds;
    }

    public final String getPlaylistsFolder() {
        return playlistsFolder;
    }

    public final boolean shouldRepeat() {
        return repeat;
    }

    public final void setRepeat(boolean repeat) {
        this.repeat = repeat;
        manager.writeSettings();
    }

    public final boolean isAutoModeration() {
        return autoModeration;
    }

    public final void setAutoModeration(boolean autoModeration) {
        this.autoModeration = autoModeration;
        manager.writeSettings();
    }

    public final String getModerationRulesFolder() {
        return moderationRulesFolder;
    }

    public final String getMaxTime() {
        final int maxTimeMultiplier = 1000;
        return FormatUtils.formatTime(maxSeconds * maxTimeMultiplier);
    }

    public final boolean isTooLong(AudioTrack track) {
        final float trackDurationDivider = 1000f;
        return (maxSeconds > 0) && (Math.round(track.getDuration() / trackDurationDivider) > maxSeconds);
    }

    public final String getRotationFolder() {
        return rotationListFolder;
    }

    public boolean shouldRotateActionsAndGames() {
        return rotateActionsAndGames;
    }

    public final void setRotateActionsAndGames(boolean rotateActionAndGames) {
        this.rotateActionsAndGames = rotateActionAndGames;
        manager.writeSettings();
    }

    public final boolean shouldRotateTextBackup() {
        return rotateTextBackup;
    }

    public final void setRotateTextBackup(boolean rotateTextBackup) {
        this.rotateTextBackup = rotateTextBackup;
        manager.writeSettings();
    }

    public final boolean shouldRotateMediaBackup() {
        return rotateMediaBackup;
    }

    public final void setRotateMediaBackup(boolean rotateMediaBackup) {
        this.rotateMediaBackup = rotateMediaBackup;
        manager.writeSettings();
    }

    public final int getTargetHourForTextBackup() {
        return targetHourForTextBackup;
    }

    public final void setTargetHourForTextBackup(int targetHourForTextBackup) {
        this.targetHourForTextBackup = targetHourForTextBackup;
        manager.writeSettings();
    }

    public final int getTargetHourForMediaBackup() {
        return targetHourForMediaBackup;
    }

    public final void setTargetHourForMediaBackup(int targetHourForMediaBackup) {
        this.targetHourForMediaBackup = targetHourForMediaBackup;
        manager.writeSettings();
    }

    public final int getDelayDaysForTextBackup() {
        return delayDaysForTextBackup;
    }

    public void setDelayDaysForTextBackup(int delayDaysForTextBackup) {
        this.delayDaysForTextBackup = delayDaysForTextBackup;
        manager.writeSettings();
    }

    public final int getDelayDaysForMediaBackup() {
        return delayDaysForMediaBackup;
    }

    public void setDelayDaysForMediaBackup(int delayDaysForMediaBackup) {
        this.delayDaysForMediaBackup = delayDaysForMediaBackup;
        manager.writeSettings();
    }
}