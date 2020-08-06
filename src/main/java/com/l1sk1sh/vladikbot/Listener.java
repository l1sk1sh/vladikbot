package com.l1sk1sh.vladikbot;

import com.l1sk1sh.vladikbot.models.entities.Reminder;
import com.l1sk1sh.vladikbot.settings.BotSettings;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Removal of update
 * - Addition of permission handler
 * - Addition of multiple services executors and verifications
 * @author John Grosh
 */
class Listener extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Listener.class);

    private final Bot bot;
    private final BotSettings botSettings;

    Listener(Bot bot) {
        this.bot = bot;
        this.botSettings = bot.getBotSettings();
    }

    @Override
    public void onReady(ReadyEvent event) {
        /* Check if bot added to Guilds */
        if (event.getJDA().getGuilds().isEmpty()) {
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(Const.RECOMMENDED_PERMS));
        }

        /* Check if Docker is running */
        if (!bot.getDockerService().isDockerRunning()) {
            log.warn("Docker is not running or not properly setup on current computer. All docker required features won't work.");
            bot.setDockerRunning(false);
        } else {
            bot.setDockerRunning(true);
        }

        /* Setup audio player */
        event.getJDA().getGuilds().forEach((guild) ->
        {
            try {
                String defaultPlaylist = bot.getGuildSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = bot.getGuildSettings(guild).getVoiceChannel(guild);
                if (defaultPlaylist != null && vc != null && bot.getPlayerManager().setUpHandler(guild).playFromDefault()) {
                    guild.getAudioManager().openAudioConnection(vc);
                }
            } catch (Exception ignore) { /* Ignore */ }

            List<Permission> missingPermissions =
                    BotUtils.getMissingPermissions(guild.getSelfMember().getPermissions(), Const.RECOMMENDED_PERMS);
            if (missingPermissions != null) {
                log.warn("Bot in guild '{}' doesn't have following recommended permissions '{}'.",
                        guild.getName(), Arrays.toString(missingPermissions.toArray()));
            }
        });

        /* Initiate automatic background backup */
        if (botSettings.shouldAutoTextBackup() || botSettings.shouldAutoMediaBackup()) {
            int minimumTimeDifference = 1;
            int maximumDayHour = 23;
            int defaultTextBackupDaysDelay = 2;
            int defaultMediaBackupDaysDelay = 7;
            int defaultTextBackupTargetHour = 10;
            int defaultMediaBackupTargetHour = 12;
            int extensionMediaHoursDelay = 2;

            if (botSettings.getDelayDaysForAutoTextBackup() <= 0) {
                log.warn("Auto text backup delay should be more than 0.");
                botSettings.setDelayDaysForAutoTextBackup(defaultTextBackupDaysDelay);
            }

            if (botSettings.getDelayDaysForAutoMediaBackup() <= 0) {
                log.warn("Auto text backup delay should be more than 0.");
                botSettings.setDelayDaysForAutoMediaBackup(defaultMediaBackupDaysDelay);
            }

            if (botSettings.getTargetHourForAutoTextBackup() > maximumDayHour || botSettings.getTargetHourForAutoTextBackup() < 0) {
                log.warn("Allowed target hour for text backup is between 1 and 24.");
                botSettings.setTargetHourForAutoTextBackup(defaultTextBackupTargetHour);
            }

            if (botSettings.getTargetHourForAutoMediaBackup() > maximumDayHour || botSettings.getTargetHourForAutoMediaBackup() < 0) {
                log.warn("Allowed target hour for media backup is between 1 and 24.");
                botSettings.setTargetHourForAutoTextBackup(defaultMediaBackupTargetHour);
            }

            int timeDifference = Math.abs(botSettings.getTargetHourForAutoTextBackup() - botSettings.getTargetHourForAutoMediaBackup());
            if (timeDifference < minimumTimeDifference) {
                log.warn("Auto backups should have at least 1 hour difference.");
                botSettings.setTargetHourForAutoMediaBackup(botSettings.getTargetHourForAutoMediaBackup() + extensionMediaHoursDelay);
            }
        }

        if (botSettings.shouldAutoMediaBackup() && bot.isDockerRunning()) {
            log.info("Enabling auto media backup service...");
            bot.getAutoMediaBackupDaemon().start();
        }

        if (botSettings.shouldAutoTextBackup() && bot.isDockerRunning()) {
            log.info("Enabling auto text backup service...");
            bot.getAutoTextBackupDaemon().start();
        }

        /* Setup game and action simulation for bot's status */
        if (botSettings.shouldSimulateActionsAndGamesActivity()) {
            log.info("Enabling GAASimulation...");
            try {
                bot.getGameAndActionSimulationManager().start();
            } catch (IOException ioe) {
                log.error("Failed to enable GAASimulation:", ioe);
                botSettings.setAutoReply(false);
            }
        }

        /* Read saved reminders and re-schedule them */
        List<Reminder> reminders = bot.getReminderService().getAllReminders();
        if (reminders != null && !reminders.isEmpty()) {
            for (Reminder reminder : reminders) {
                boolean scheduled = bot.getReminderService().scheduleReminder(reminder);
                if (!scheduled) {
                    reminder.setDateOfReminder(new Date(System.currentTimeMillis() + (60 * 1000)));
                    bot.getReminderService().scheduleReminder(reminder);
                }
            }
        }

        /* Initiate RSS feed reader */
        if (botSettings.shouldSendNews()) {
            bot.getRssService().start();
        }

        /* Initiate memes fetcher*/
        if (botSettings.shouldSendNews()) {
            bot.getMemeService().start();
        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        bot.getNowPlayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());

        if (bot.getBotSettings().shouldLogGuildChanges()) {
            bot.getGuildLoggerService().onMessageDelete(event);
        }
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (event.getMessage().getAuthor().isBot()) {
            return;
        }

        if (bot.getBotSettings().shouldLogGuildChanges()) {
            bot.getGuildLoggerService().onMessageUpdate(event);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (message.getAuthor().isBot()) {
            return;
        }

        if (bot.getBotSettings().shouldAutoReply()) {
            bot.getAutoReplyManager().reply(message);
        }

        if (bot.getBotSettings().shouldLogGuildChanges()) {
            bot.getMessageCache().putMessage(message);
        }
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        if (!event.getUser().isBot() && bot.getBotSettings().shouldLogGuildChanges()) {
            bot.getGuildLoggerService().onAvatarUpdate(event);
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        bot.shutdown();
    }
}
