package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.services.backup.BackupMediaService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class BackupMediaCommand extends AdminCommand {

    private BackupMediaCommand(BotSettingsManager settings, @Qualifier("backgroundThreadPool") ScheduledExecutorService backgroundThreadPool, BackupMediaService backupMediaService) {
        this.name = "backup_media";
        this.help = "Media backup management";
        this.children = new AdminCommand[]{
                new Auto(settings),
                new Do(backgroundThreadPool, backupMediaService),
                new Export(backgroundThreadPool, backupMediaService),
                new Reset(backupMediaService)
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString()).setEphemeral(true).queue();
    }

    private static final class Auto extends AdminCommand {

        private static final String AUTO_OPTION_KEY = "auto";

        private final BotSettingsManager settings;

        private Auto(BotSettingsManager settings) {
            this.settings = settings;
            this.name = "auto";
            this.help = "Configure automatic backup";
            this.guildOnly = false;
            this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, AUTO_OPTION_KEY, "State of automatic backup").setRequired(false));
        }

        @SuppressWarnings("DuplicatedCode")
        @Override
        protected void execute(SlashCommandEvent event) {
            boolean currentSetting = settings.get().isAutoMediaBackup();

            OptionMapping autoOption = event.getOption(AUTO_OPTION_KEY);
            if (autoOption == null) {
                event.replyFormat("Automatic backup is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();

                return;
            }

            boolean newSetting = autoOption.getAsBoolean();

            if (currentSetting == newSetting) {
                event.replyFormat("Automatic backup is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();

                return;
            }

            settings.get().setAutoMediaBackup(newSetting);

            log.info("Automatic backup is switched to {} by {}.", newSetting, FormatUtils.formatAuthor(event));
            event.replyFormat("Automatic backup is `%1$s`", (newSetting) ? "ON" : "OFF").setEphemeral(true).queue();
        }
    }

    private static final class Do extends AdminCommand {

        private final ScheduledExecutorService backgroundThreadPool;
        private final BackupMediaService backupMediaService;

        private Do(ScheduledExecutorService backgroundThreadPool, BackupMediaService backupMediaService) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.backupMediaService = backupMediaService;
            this.name = "do";
            this.help = "Launch media backup";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                log.info("Media backup started by {}", FormatUtils.formatAuthor(event));
                event.deferReply(true).queue();
                backupMediaService.downloadAllAttachments((success, message) -> {
                    if (success) {
                        event.getHook().editOriginalFormat("%1$s Media was downloaded!", event.getClient().getSuccess()).queue();
                    } else {
                        event.getHook().editOriginalFormat("%1$s Media was not downloaded! (%2$s)", event.getClient().getError(), message).queue();
                    }
                });
            });
        }
    }

    private static final class Export extends AdminCommand {

        private final ScheduledExecutorService backgroundThreadPool;
        private final BackupMediaService backupMediaService;

        private Export(ScheduledExecutorService backgroundThreadPool, BackupMediaService backupMediaService) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.backupMediaService = backupMediaService;
            this.name = "export";
            this.help = "Export media files of the current channel to html file";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                log.info("Media export started by {}", FormatUtils.formatAuthor(event));
                event.deferReply(true).queue();
                backupMediaService.exportMediaToHtmlFile((success, message, file) -> {
                    if (success) {
                        event.getHook().editOriginalFormat("%1$s Export is ready!", event.getClient().getSuccess()).setFiles(FileUpload.fromData(file)).queue();
                    } else {
                        event.getHook().editOriginalFormat("%1$s Export has failed! (%2$s)", event.getClient().getError(), message).queue();
                    }
                }, event.getChannel().getIdLong());
            });
        }
    }

    private static final class Reset extends AdminCommand {

        private final BackupMediaService backupMediaService;

        private Reset(BackupMediaService backupMediaService) {
            this.backupMediaService = backupMediaService;
            this.name = "reset";
            this.help = "Resets database of already downloaded attachments. Necessary if local files were lost";
            this.ownerCommand = true;
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backupMediaService.resetDownloadedAttachments();
            log.info("Media reset started by {}", FormatUtils.formatAuthor(event));
            event.replyFormat("%1$s Reset has been started. For details consult logs", event.getClient().getSuccess()).setEphemeral(true).queue();
        }
    }
}
