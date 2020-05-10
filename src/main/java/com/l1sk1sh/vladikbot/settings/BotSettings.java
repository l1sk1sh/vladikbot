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
public class BotSettings {
    private transient BotSettingsManager manager;

    /* Finish all paths with file system separator! */
    private String token = "MY_BOT_TOKEN";                              // Bot token taken from discord developer portal
    private long ownerId = 0L;                                          // Id of the person, who is hosting the bot
    private long maintainerGuildId = 0L;                                // Id of Guild that will be used to maintaining notifs
    private String localTmpFolder = "./app/tmp/";                       // Local tmp for workdir
    private String rotationBackupFolder = "./app/backup/";              // Local rotation backup folder (that will be stored)
    private String playlistsFolder = "./app/playlists/";                // Local folder for playlists to be stored
    private String rulesFolder = "./app/rules/";                        // Local storage for automoderation settings
    private String logsFolder = "./app/logs/";                          // Local storage for guild logging
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
    private boolean autoReply = false;                                  // If to automatically reply to certain phrases
    private Const.MatchingStrategy matchingStrategy = Const.MatchingStrategy.full; // How reply rules should be matched
    private double replyChange = 1.0;                                   // Change that bot will reply
    private boolean simulateActionAndGamesActivity = false;             // If bot should change statuses by himself
    private boolean logGuildChanges = false;                            // If bot should log message/avatars etc changes
    private boolean autoTextBackup = true;                              // Automatically create backups of all available chats
    private boolean autoMediaBackup = true;                             // Automatically save media from all available chats
    private int targetHourForAutoTextBackup = 0;                        // Set local TZ hour for text backup to be started
    private int targetHourForAutoMediaBackup = 0;                       // Set local TZ hour for media backup to be started
    private int delayDaysForAutoTextBackup = 0;                         // Set delay in days between text backups
    private int delayDaysForAutoMediaBackup = 0;                        // Set delay in days between text backups
    private String dockerHost = "tcp://localhost:2375";                 // Set custom docker host

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

    public long getMaintainerGuildId() {
        return maintainerGuildId;
    }

    public final String getLocalTmpFolder() {
        return localTmpFolder;
    }

    public String getRotationBackupFolder() {
        return rotationBackupFolder;
    }

    public String getLogsFolder() {
        return logsFolder;
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

    public final boolean shouldAutoReply() {
        return autoReply;
    }

    public final void setAutoReply(boolean autoReply) {
        this.autoReply = autoReply;
        manager.writeSettings();
    }

    public Const.MatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    public void setMatchingStrategy(Const.MatchingStrategy matchingStrategy) {
        this.matchingStrategy = matchingStrategy;
        manager.writeSettings();
    }

    public double getReplyChance() {
        return replyChange;
    }

    public void setReplyChange(double replyChange) {
        this.replyChange = replyChange;
        manager.writeSettings();
    }

    public final String getRulesFolder() {
        return rulesFolder;
    }

    public final String getMaxTime() {
        final int maxTimeMultiplier = 1000;
        return FormatUtils.formatTimeTillHours(maxSeconds * maxTimeMultiplier);
    }

    public final boolean isTooLong(AudioTrack track) {
        final float trackDurationDivider = 1000f;
        return (maxSeconds > 0) && (Math.round(track.getDuration() / trackDurationDivider) > maxSeconds);
    }

    public boolean shouldSimulateActionsAndGamesActivity() {
        return simulateActionAndGamesActivity;
    }

    public final void setSimulateActionAndGameActivity(boolean simulateActionAndGamesActivity) {
        this.simulateActionAndGamesActivity = simulateActionAndGamesActivity;
        manager.writeSettings();
    }

    public boolean shouldLogGuildChanges() {
        return logGuildChanges;
    }

    public void setLogGuildChanges(boolean logGuildChanges) {
        this.logGuildChanges = logGuildChanges;
        manager.writeSettings();
    }

    public final boolean shouldAutoTextBackup() {
        return autoTextBackup;
    }

    public final void setAutoTextBackup(boolean autoTextBackup) {
        this.autoTextBackup = autoTextBackup;
        manager.writeSettings();
    }

    public final boolean shouldAutoMediaBackup() {
        return autoMediaBackup;
    }

    public final void setAutoMediaBackup(boolean autoMediaBackup) {
        this.autoMediaBackup = autoMediaBackup;
        manager.writeSettings();
    }

    public final int getTargetHourForAutoTextBackup() {
        return targetHourForAutoTextBackup;
    }

    public final void setTargetHourForAutoTextBackup(int targetHourForAutoTextBackup) {
        this.targetHourForAutoTextBackup = targetHourForAutoTextBackup;
        manager.writeSettings();
    }

    public final int getTargetHourForAutoMediaBackup() {
        return targetHourForAutoMediaBackup;
    }

    public final void setTargetHourForAutoMediaBackup(int targetHourForAutoMediaBackup) {
        this.targetHourForAutoMediaBackup = targetHourForAutoMediaBackup;
        manager.writeSettings();
    }

    public final int getDelayDaysForAutoTextBackup() {
        return delayDaysForAutoTextBackup;
    }

    public void setDelayDaysForAutoTextBackup(int delayDaysForAutoTextBackup) {
        this.delayDaysForAutoTextBackup = delayDaysForAutoTextBackup;
        manager.writeSettings();
    }

    public final int getDelayDaysForAutoMediaBackup() {
        return delayDaysForAutoMediaBackup;
    }

    public void setDelayDaysForAutoMediaBackup(int delayDaysForMediaBackup) {
        this.delayDaysForAutoMediaBackup = delayDaysForMediaBackup;
        manager.writeSettings();
    }

    public String getDockerHost() {
        return dockerHost;
    }
}