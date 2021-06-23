package com.l1sk1sh.vladikbot;

import com.google.gson.Gson;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.entity.Reminder;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.ReminderService;
import com.l1sk1sh.vladikbot.services.ShutdownHandler;
import com.l1sk1sh.vladikbot.services.audio.AloneInVoiceHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.services.backup.AutoMediaBackupDaemon;
import com.l1sk1sh.vladikbot.services.backup.AutoTextBackupDaemon;
import com.l1sk1sh.vladikbot.services.backup.DockerService;
import com.l1sk1sh.vladikbot.services.logging.GuildLoggerService;
import com.l1sk1sh.vladikbot.services.logging.MessageCache;
import com.l1sk1sh.vladikbot.services.meme.MemeService;
import com.l1sk1sh.vladikbot.services.presence.ActivitySimulationManager;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.services.rss.RssService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.MigrationUtils;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - Removal of update
 * - Addition of permission handler
 * - Addition of multiple services executors and verifications
 * - DI Spring
 * @author John Grosh
 */
@RequiredArgsConstructor
@Service
class Listener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    private final Gson gson;
    private final ShutdownHandler shutdownHandler;
    private final BotSettingsManager settings;
    private final DockerService dockerService;
    private final PlayerManager playerManager;
    private final ReminderService reminderService;
    private final RssService rssService;
    private final MemeService memeService;
    private final AutoTextBackupDaemon autoTextBackupDaemon;
    private final AutoMediaBackupDaemon autoMediaBackupDaemon;
    private final GuildLoggerService guildLoggerService;
    private final NowPlayingHandler nowPlayingHandler;
    private final MessageCache messageCache;
    private final AloneInVoiceHandler aloneInVoiceHandler;
    private final AutoReplyManager autoReplyManager;
    private final GuildSettingsRepository guildSettingsRepository;
    private final ActivitySimulationManager activitySimulationManager;

    @Override
    public void onReady(@NotNull ReadyEvent event) {

        /* Execute migrations if necessary */
        if (!FileUtils.fileOrFolderIsAbsent("./" + MigrationUtils.REPLIES_FILE_NAME)) {
            log.debug("Migrating .json file with reply rules to database...");
            MigrationUtils.migrateReplyRules(autoReplyManager, gson);
        }

        if (!FileUtils.fileOrFolderIsAbsent("./" + MigrationUtils.SIMULATIONS_FILE_NAME)) {
            log.debug("Migrating .json file with activities to database...");
            MigrationUtils.migrateActivities(activitySimulationManager, gson);
        }

        /* Check if bot added to Guilds */
        if (event.getJDA().getGuilds().isEmpty()) {
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(Const.RECOMMENDED_PERMS));
        }

        /* Setting default activity and status */
        event.getJDA().getPresence().setActivity((settings.get().getActivity() != null)
                ? settings.get().getActivity() : Activity.playing("Type " + settings.get().getPrefix() + "help for commands"));
        event.getJDA().getPresence().setStatus((settings.get().getOnlineStatus() != OnlineStatus.UNKNOWN)
                ? settings.get().getOnlineStatus() : OnlineStatus.DO_NOT_DISTURB);

        /* Create settings for new Guilds */
        for (Guild guild : event.getJDA().getGuilds()) {
            Optional<GuildSettings> settings = guildSettingsRepository.findById(guild.getIdLong());
            if (settings.isEmpty()) {
                GuildSettings newSettings = new GuildSettings();
                newSettings.setGuildId(guild.getIdLong());
                guildSettingsRepository.save(newSettings);
            }
        }

        /* Setup audio player */
        event.getJDA().getGuilds().forEach((guild) -> {
            try {
                String defaultPlaylist = guildSettingsRepository.getOne(guild.getIdLong()).getDefaultPlaylist();
                VoiceChannel vc = guildSettingsRepository.getOne(guild.getIdLong()).getVoiceChannel(guild);
                if (defaultPlaylist != null && vc != null && playerManager.setUpHandler(guild).playFromDefault()) {
                    guild.getAudioManager().openAudioConnection(vc);
                }
            } catch (Exception ignored) {
            }

            List<Permission> missingPermissions =
                    BotUtils.getMissingPermissions(guild.getSelfMember().getPermissions(), Const.RECOMMENDED_PERMS);
            if (missingPermissions != null) {
                log.warn("Bot in guild '{}' doesn't have following recommended permissions '{}'.",
                        guild.getName(), Arrays.toString(missingPermissions.toArray()));
            }
        });

        /* Read saved reminders and re-schedule them */
        List<Reminder> reminders = reminderService.getAllReminders();
        if (reminders != null && !reminders.isEmpty()) {
            for (Reminder reminder : reminders) {
                boolean scheduled = reminderService.scheduleReminder(reminder);
                if (!scheduled) {
                    reminder.setDateOfReminder(new Date(System.currentTimeMillis() + (60 * 1000)));
                    reminderService.scheduleReminder(reminder);
                }
            }
        }

        /* Setup activity simulation for bot's status */
        if (settings.get().isSimulateActivity()) {
            activitySimulationManager.start();
        }

        /* Initiate RSS feed reader */
        if (settings.get().isSendNews()) {
            rssService.start();
        }

        /* Initiate memes fetcher*/
        if (settings.get().isSendMemes()) {
            memeService.start();
        }

        /* Check if Docker is running */
        if (!dockerService.isDockerRunning()) {
            log.warn("Docker is not running or not properly setup on current computer. All docker required features won't work.");
            settings.get().setDockerRunning(false);
        } else {
            settings.get().setDockerRunning(true);
        }

        /* Initiate automatic background backup */
        if (settings.get().isAutoTextBackup() || settings.get().isAutoMediaBackup()) {
            int minimumTimeDifference = 1;
            int maximumDayHour = 23;
            int defaultTextBackupDaysDelay = 2;
            int defaultMediaBackupDaysDelay = 7;
            int defaultTextBackupTargetHour = 10;
            int defaultMediaBackupTargetHour = 12;
            int extensionMediaHoursDelay = 2;

            if (settings.get().getDelayDaysForAutoTextBackup() <= 0) {
                log.warn("Auto text backup delay should be more than 0.");
                settings.get().setDelayDaysForAutoTextBackup(defaultTextBackupDaysDelay);
            }

            if (settings.get().getDelayDaysForAutoMediaBackup() <= 0) {
                log.warn("Auto text backup delay should be more than 0.");
                settings.get().setDelayDaysForAutoMediaBackup(defaultMediaBackupDaysDelay);
            }

            if (settings.get().getTargetHourForAutoTextBackup() > maximumDayHour || settings.get().getTargetHourForAutoTextBackup() < 0) {
                log.warn("Allowed target hour for text backup is between 1 and 24.");
                settings.get().setTargetHourForAutoTextBackup(defaultTextBackupTargetHour);
            }

            if (settings.get().getTargetHourForAutoMediaBackup() > maximumDayHour || settings.get().getTargetHourForAutoMediaBackup() < 0) {
                log.warn("Allowed target hour for media backup is between 1 and 24.");
                settings.get().setTargetHourForAutoTextBackup(defaultMediaBackupTargetHour);
            }

            int timeDifference = Math.abs(settings.get().getTargetHourForAutoTextBackup() - settings.get().getTargetHourForAutoMediaBackup());
            if (timeDifference < minimumTimeDifference) {
                log.warn("Auto backups should have at least 1 hour difference.");
                settings.get().setTargetHourForAutoMediaBackup(settings.get().getTargetHourForAutoMediaBackup() + extensionMediaHoursDelay);
            }
        }

        if (settings.get().isAutoMediaBackup() && settings.get().isDockerRunning()) {
            autoMediaBackupDaemon.start();
        }

        if (settings.get().isAutoTextBackup() && settings.get().isDockerRunning()) {
            autoTextBackupDaemon.start();
        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        nowPlayingHandler.onMessageDelete(event.getGuild(), event.getMessageIdLong());

        if (settings.get().isLogGuildChanges()) {
            guildLoggerService.onMessageDelete(event);
        }
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (event.getMessage().getAuthor().isBot()) {
            return;
        }

        if (settings.get().isLogGuildChanges()) {
            guildLoggerService.onMessageUpdate(event);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (message.getAuthor().isBot()) {
            return;
        }

        if (settings.get().isAutoReply()) {
            autoReplyManager.reply(message);
        }

        if (settings.get().isLogGuildChanges()) {
            messageCache.putMessage(message);
        }
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        if (!event.getUser().isBot() && settings.get().isLogGuildChanges()) {
            guildLoggerService.onAvatarUpdate(event);
        }
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        aloneInVoiceHandler.onVoiceUpdate(event);
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        shutdownHandler.shutdown();
    }
}
