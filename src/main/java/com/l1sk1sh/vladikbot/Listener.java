package com.l1sk1sh.vladikbot;

import com.l1sk1sh.vladikbot.settings.BotSettings;
import com.l1sk1sh.vladikbot.settings.Const;
import com.l1sk1sh.vladikbot.utils.BotUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Removal of update
 * - Addition of moderation Listener
 * - Addition of permission handler
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
        if (event.getJDA().getGuilds().isEmpty()) {
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().asBot().getInviteUrl(Const.RECOMMENDED_PERMS));
        }

        if (!bot.getDockerVerificationService().isDockerRunning()) {
            log.warn("Docker is not running or not properly setup on current computer. All docker required features won't work.");
            bot.setDockerFailed(true);
        }

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
                log.warn("Bot in guild '{}' doesn't have following recommended permissions {}.",
                        guild.getName(), Arrays.toString(missingPermissions.toArray()));
            }
        });

        // TODO Maybe, this should be moved to settingsManager or rotationManager
        if (botSettings.shouldRotateTextBackup() || botSettings.shouldRotateMediaBackup()) {
            int minimumTimeDifference = 1;
            int maximumDayHour = 23;
            int defaultTextBackupDaysDelay = 2;
            int defaultMediaBackupDaysDelay = 7;
            int defaultTextBackupTargetHour = 10;
            int defaultMediaBackupTargetHour = 12;
            int extensionMediaHoursDelay = 2;

            if (botSettings.getDelayDaysForTextBackup() <= 0) {
                log.warn("Rotation text backup delay should be more than 0");
                botSettings.setDelayDaysForTextBackup(defaultTextBackupDaysDelay);
            }

            if (botSettings.getDelayDaysForMediaBackup() <= 0) {
                log.warn("Rotation text backup delay should be more than 0");
                botSettings.setDelayDaysForMediaBackup(defaultMediaBackupDaysDelay);
            }

            if (botSettings.getTargetHourForTextBackup() > maximumDayHour || botSettings.getTargetHourForTextBackup() < 0) {
                log.warn("Allowed target hour for text backup is between 1 and 24");
                botSettings.setTargetHourForTextBackup(defaultTextBackupTargetHour);
            }

            if (botSettings.getTargetHourForMediaBackup() > maximumDayHour || botSettings.getTargetHourForMediaBackup() < 0) {
                log.warn("Allowed target hour for media backup is between 1 and 24");
                botSettings.setTargetHourForTextBackup(defaultMediaBackupTargetHour);
            }

            int timeDifference = Math.abs(botSettings.getTargetHourForTextBackup() - botSettings.getTargetHourForMediaBackup());
            if (timeDifference < minimumTimeDifference) {
                log.warn("Rotation backups should have at least 1 hour difference");
                botSettings.setTargetHourForMediaBackup(botSettings.getTargetHourForMediaBackup() + extensionMediaHoursDelay);
            }
        }

        if (botSettings.shouldRotateMediaBackup()) {
            log.info("Enabling Rotation media backup service...");
            bot.getRotatingMediaBackupDaemon().enableExecution();
        }

        if (botSettings.shouldRotateTextBackup()) {
            log.info("Enabling Rotation text backup service...");
            bot.getRotatingTextBackupDaemon().enableExecution();
            bot.getRotatingTextBackupDaemon().execute();
        }

        if (botSettings.shouldRotateActionsAndGames()) {
            log.info("Enabling Rotation of Action and Game");
            bot.getActionAndGameRotationManager().activateRotation();
        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        bot.getNowPlayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();

        if (!message.getAuthor().isBot()) {
            bot.getAutoModerationManager().moderate(message);
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        bot.shutdown();
    }
}
