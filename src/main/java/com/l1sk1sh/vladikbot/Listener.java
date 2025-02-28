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
import com.l1sk1sh.vladikbot.services.backup.BackupMediaService;
import com.l1sk1sh.vladikbot.services.backup.BackupTextService;
import com.l1sk1sh.vladikbot.services.logging.GuildLoggerService;
import com.l1sk1sh.vladikbot.services.logging.MessageCache;
import com.l1sk1sh.vladikbot.services.meme.MemeService;
import com.l1sk1sh.vladikbot.services.presence.ActivitySimulationManager;
import com.l1sk1sh.vladikbot.services.presence.AutoReplyManager;
import com.l1sk1sh.vladikbot.services.rss.RssService;
import com.l1sk1sh.vladikbot.services.youtube.YouTubeSessionGenerator;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import com.l1sk1sh.vladikbot.utils.FileUtils;
import com.l1sk1sh.vladikbot.utils.MigrationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
@Slf4j
@RequiredArgsConstructor
@Service
class Listener extends ListenerAdapter {

    private final Gson gson;
    private final ShutdownHandler shutdownHandler;
    private final BotSettingsManager settings;
    private final PlayerManager playerManager;
    private final ReminderService reminderService;
    private final RssService rssService;
    private final MemeService memeService;
    private final BackupTextService backupTextService;
    private final BackupMediaService backupMediaService;
    private final AutoTextBackupDaemon autoTextBackupDaemon;
    private final AutoMediaBackupDaemon autoMediaBackupDaemon;
    private final GuildLoggerService guildLoggerService;
    private final NowPlayingHandler nowPlayingHandler;
    private final MessageCache messageCache;
    private final AloneInVoiceHandler aloneInVoiceHandler;
    private final AutoReplyManager autoReplyManager;
    private final GuildSettingsRepository guildSettingsRepository;
    private final ActivitySimulationManager activitySimulationManager;
    private final YouTubeSessionGenerator youTubeSessionGenerator;

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
        List<Guild> connectedGuilds = event.getJDA().getGuilds();
        if (connectedGuilds.isEmpty()) {
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(Const.RECOMMENDED_PERMS));
        }

        /* Setting default activity and status */
        event.getJDA().getPresence().setActivity((settings.get().getActivity() != null)
                ? settings.get().getActivity() : Activity.playing("Type " + settings.get().getPrefix() + "help for commands"));
        event.getJDA().getPresence().setStatus((settings.get().getOnlineStatus() != OnlineStatus.UNKNOWN)
                ? settings.get().getOnlineStatus() : OnlineStatus.DO_NOT_DISTURB);

        /* Create settings for new Guilds */
        for (Guild guild : connectedGuilds) {
            if (!guildSettingsRepository.existsById(guild.getIdLong())) {
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
                    guild.getAudioManager().setSelfDeafened(true);
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
                    log.error(reminderService.getErrorMessage());
                    reminderService.deleteReminder(reminder.getId());
                }
            }
        }

        /* Setup activity simulation for bot's status */
        if (settings.get().isSimulateActivity()) {
            activitySimulationManager.start();
        }

        /* Initiate RSS feed reader */
        if (!guildSettingsRepository.getAllBySendNewsIsTrue().isEmpty()) {
            rssService.start();
        }

        /* Initiate memes fetcher*/
        if (!guildSettingsRepository.getAllBySendMemesIsTrue().isEmpty()) {
            memeService.start();
        }

        /* Prepare backup services */
        backupMediaService.init();

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

        if (settings.get().isAutoTextBackup()) {
            autoTextBackupDaemon.start();
        }

        if (settings.get().isAutoMediaBackup()) {
            autoMediaBackupDaemon.start();
        }

        /* Set tokens from YouTube session generator */
        youTubeSessionGenerator.fetchAndSetYtSession();
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        nowPlayingHandler.onMessageDelete(event.getGuild(), event.getMessageIdLong());

        if (!guildSettingsRepository.getAllByLogGuildChangesIsTrue().isEmpty()) {
            guildLoggerService.onMessageDelete(event);
        }
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.getMessage().getAuthor().isBot()) {
            return;
        }

        if (!guildSettingsRepository.getAllByLogGuildChangesIsTrue().isEmpty()) {
            guildLoggerService.onMessageUpdate(event);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();

        backupTextService.backupNewMessage(message);

        if (message.getAuthor().isBot()) {
            return;
        }

        if (guildSettingsRepository.findById(event.getGuild().getIdLong()).map(GuildSettings::isAutoReply).orElse(false)) {
            autoReplyManager.reply(message);
        }

        if (!guildSettingsRepository.getAllByLogGuildChangesIsTrue().isEmpty()) {
            messageCache.putMessage(message);
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) {
            return;
        }

        backupTextService.addReaction(event.getReaction());
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
