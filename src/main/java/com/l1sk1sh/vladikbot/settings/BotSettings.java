package com.l1sk1sh.vladikbot.settings;

import com.l1sk1sh.vladikbot.models.AudioRepeatMode;
import com.l1sk1sh.vladikbot.models.queue.QueueType;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

/**
 * @author l1sk1sh
 */
@SuppressWarnings({"CanBeFinal", "FieldMayBeFinal"})
@Getter
public class BotSettings {

    @Getter(AccessLevel.NONE)
    @Setter
    private transient SettingsUpdateListener listener;

    private String token = "MY_BOT_TOKEN";                              // Bot token taken from discord developer portal
    private long ownerId = 0L;                                          // Id of the person, who is hosting the bot
    private long maintainerGuildId = 0L;                                // Id of Guild that will be used to maintaining notifications
    private long forceGuildId = 0L;                                     // Id of Guild that will be used for fast commands update (single guild or debug)
    private String workdir = "./app";                                   // Working directory for all files
    private String prefix = "~";                                        // Bot prefix
    private String successEmoji = "\uD83D\uDC4C";                       // 👌
    private String warningEmoji = "\uD83D\uDD95";                       // 🖕
    private String errorEmoji = "\uD83D\uDCA2";                         // 💢
    private String loadingEmoji = "\uD83E\uDDF6";                       // 🧶
    private String searchingEmoji = "\uD83D\uDD0E";                     // 🔎
    private String activity = "watching Ubisoft conference";            // Current name of the 'activity' being done by bot
    private String onlineStatus = "ONLINE";                             // "online", "idle", "dnd", "invisible", "offline", ""
    private long maxSeconds = 0L;                                       // Maximum song length
    private long aloneTimeUntilStop = 0L;                               // Time until bot leaves voice channel if alone
    private boolean leaveChannel = true;                                // Leave channel if no one is listening
    private boolean npImages = true;                                    // Display search images
    private AudioRepeatMode repeat = AudioRepeatMode.OFF;               // Current repeat mode
    private QueueType queueType = QueueType.FAIR;                       // Queue type
    private double audioSkipRatio = 0.55;                               // Voting ratio to skip current song
    private boolean simulateActivity = false;                           // If bot should change statuses by himself
    private boolean autoTextBackup = true;                              // Automatically create backups of all available chats
    private boolean autoMediaBackup = true;                             // Automatically save media from all available chats
    private int targetHourForAutoTextBackup = 0;                        // Set local TZ hour for text backup to be started
    private int targetHourForAutoMediaBackup = 0;                       // Set local TZ hour for media backup to be started
    private int delayDaysForAutoTextBackup = 0;                         // Set delay in days between text backups
    private int delayDaysForAutoMediaBackup = 0;                        // Set delay in days between text backups
    private String jenkinsApiHost = "http://127.0.0.1:8080";            // Set jenkins API host
    private String jenkinsApiUsername = "bot";                          // Set jenkins API username
    private String jenkinsApiPassword = "JENKINS_API_TOKEN";            // Set jenkins API password
    private long lastAutoTextBackupTime = 0L;                           // Holds time of last auto text backup
    private long lastAutoMediaBackupTime = 0L;                          // Holds time of last auto media backup
    private String botPublicIP = "";                                    // Holds public IP that might be used to access local servers
    private String spClientId = "";                                     // Holds Spotify App Client ID
    private String spClientSecret = "";                                 // Holds Spotify App Client Secret
    private String spDc = "";                                           // Holds Spotify User sp_dc cookie
    private String spCountryCode = "DE";                                // Holds Spotify User Country

    /* Runtime and bot specific internal configs */
    @Setter
    private transient boolean lockedAutoBackup = false;                 // Locks automatic backup thread

    BotSettings(SettingsUpdateListener listener) {
        this.listener = listener;
    }

    public Activity getActivity() {
        return BotUtils.parseActivity(activity);
    }

    public final OnlineStatus getOnlineStatus() {
        return BotUtils.parseStatus(onlineStatus);
    }

    public final void setRepeat(AudioRepeatMode repeat) {
        this.repeat = repeat;
        listener.onSettingsUpdated();
    }

    public final void setQueueType(QueueType type) {
        this.queueType = type;
        listener.onSettingsUpdated();
    }

    public void setAudioSkipRatio(double skipRatio) {
        this.audioSkipRatio = skipRatio;
        listener.onSettingsUpdated();
    }

    public final void setSimulateActivity(boolean simulateActivity) {
        this.simulateActivity = simulateActivity;
        listener.onSettingsUpdated();
    }

    public final void setAutoTextBackup(boolean autoTextBackup) {
        this.autoTextBackup = autoTextBackup;
        listener.onSettingsUpdated();
    }

    public final void setAutoMediaBackup(boolean autoMediaBackup) {
        this.autoMediaBackup = autoMediaBackup;
        listener.onSettingsUpdated();
    }

    public final void setTargetHourForAutoTextBackup(int targetHourForAutoTextBackup) {
        this.targetHourForAutoTextBackup = targetHourForAutoTextBackup;
        listener.onSettingsUpdated();
    }

    public final void setTargetHourForAutoMediaBackup(int targetHourForAutoMediaBackup) {
        this.targetHourForAutoMediaBackup = targetHourForAutoMediaBackup;
        listener.onSettingsUpdated();
    }

    public void setDelayDaysForAutoTextBackup(int delayDaysForAutoTextBackup) {
        this.delayDaysForAutoTextBackup = delayDaysForAutoTextBackup;
        listener.onSettingsUpdated();
    }

    public void setDelayDaysForAutoMediaBackup(int delayDaysForMediaBackup) {
        this.delayDaysForAutoMediaBackup = delayDaysForMediaBackup;
        listener.onSettingsUpdated();
    }

    public void setLastAutoTextBackupTime(long lastAutoTextBackupTime) {
        this.lastAutoTextBackupTime = lastAutoTextBackupTime;
        listener.onSettingsUpdated();
    }

    public void setLastAutoMediaBackupTime(long lastAutoMediaBackupTime) {
        this.lastAutoMediaBackupTime = lastAutoMediaBackupTime;
        listener.onSettingsUpdated();
    }

    public void setBotPublicIP(String ip) {
        this.botPublicIP = ip;
        listener.onSettingsUpdated();
    }

    public final String getMaxTime() {
        final int maxTimeMultiplier = 1000;
        return FormatUtils.formatTimeTillHours(maxSeconds * maxTimeMultiplier);
    }

    public final boolean isTooLong(AudioTrack track) {
        final float trackDurationDivider = 1000f;
        return (maxSeconds > 0) && (Math.round(track.getDuration() / trackDurationDivider) > maxSeconds);
    }

    public final void updateMissingValues() {
        BotSettings defSettings = new BotSettings(listener);
        if (this.token == null) {
            this.token = defSettings.token;
        }
        /* ownerId - not setting */
        /* maintainerGuildId - not setting */
        /* forceGuildId - not setting */
        if (this.workdir == null) {
            this.workdir = defSettings.workdir;
        }
        if (this.prefix == null) {
            this.prefix = defSettings.prefix;
        }
        if (this.successEmoji == null) {
            this.successEmoji = defSettings.successEmoji;
        }
        if (this.warningEmoji == null) {
            this.warningEmoji = defSettings.warningEmoji;
        }
        if (this.errorEmoji == null) {
            this.errorEmoji = defSettings.errorEmoji;
        }
        if (this.loadingEmoji == null) {
            this.loadingEmoji = defSettings.loadingEmoji;
        }
        if (this.searchingEmoji == null) {
            this.searchingEmoji = defSettings.searchingEmoji;
        }
        if (this.activity == null) {
            this.activity = defSettings.activity;
        }
        if (this.onlineStatus == null) {
            this.onlineStatus = defSettings.onlineStatus;
        }
        if (this.searchingEmoji == null) {
            this.searchingEmoji = defSettings.searchingEmoji;
        }
        /* maxSeconds - not setting */
        /* aloneTimeUntilStop - not setting */
        /* leaveChannel - not setting */
        /* npImages - not setting */
        if (this.repeat == null) {
            this.repeat = defSettings.repeat;
        }
        if (this.queueType == null) {
            this.queueType = defSettings.queueType;
        }
        /* audioSkipRatio - not setting */
        /* simulateActivity - not setting */
        /* autoTextBackup - not setting */
        /* autoMediaBackup - not setting */
        /* targetHourForAutoTextBackup - not setting */
        /* targetHourForAutoMediaBackup - not setting */
        /* delayDaysForAutoTextBackup - not setting */
        /* delayDaysForAutoMediaBackup - not setting */
        if (this.jenkinsApiHost == null) {
            this.jenkinsApiHost = defSettings.jenkinsApiHost;
        }
        if (this.jenkinsApiUsername == null) {
            this.jenkinsApiUsername = defSettings.jenkinsApiUsername;
        }
        if (this.jenkinsApiPassword == null) {
            this.jenkinsApiPassword = defSettings.jenkinsApiPassword;
        }
        /* lastAutoTextBackupTime - not setting */
        /* lastAutoMediaBackupTime - not setting */
        if (this.botPublicIP == null) {
            this.botPublicIP = defSettings.botPublicIP;
        }
        if (this.spClientId == null) {
            this.spClientId = defSettings.spClientId;
        }
        if (this.spClientSecret == null) {
            this.spClientSecret = defSettings.spClientSecret;
        }
        if (this.spCountryCode == null) {
            this.spCountryCode = defSettings.spCountryCode;
        }
        if (this.spDc == null) {
            this.spDc = defSettings.spDc;
        }
    }
}