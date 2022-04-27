package com.l1sk1sh.vladikbot.commands.admin;

import com.l1sk1sh.vladikbot.services.backup.BackupTextService;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class BackupTextCommand extends AdminCommand {

    private BackupTextCommand(BotSettingsManager settings, ScheduledExecutorService backgroundThreadPool, BackupTextService backupTextService) {
        this.name = "backup_text";
        this.help = "Text backup management";
        this.children = new AdminCommand[]{
                new Auto(settings),
                new Do(backgroundThreadPool, backupTextService),
                new Export()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(this, children, name).toString()).setEphemeral(true).queue();
    }

    private static final class Auto extends AdminCommand {

        private static final String AUTO_OPTION_KEY = "auto";

        private final BotSettingsManager settings;

        private Auto(BotSettingsManager settings) {
            this.settings = settings;
            this.name = "auto";
            this.help = "Configure automatic backup";
            this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, AUTO_OPTION_KEY, "State of automatic backup").setRequired(false));
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            boolean currentSetting = settings.get().isAutoTextBackup();

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

            settings.get().setAutoTextBackup(newSetting);

            log.info("Automatic backup is switched to {} by {}.", newSetting, FormatUtils.formatAuthor(event));
            event.replyFormat("Automatic backup is `%1$s`", (newSetting) ? "ON" : "OFF").setEphemeral(true).queue();
        }
    }

    private static final class Do extends AdminCommand {

        private final ScheduledExecutorService backgroundThreadPool;
        private final BackupTextService backupTextService;

        private Do(ScheduledExecutorService backgroundThreadPool, BackupTextService backupTextService) {
            this.backgroundThreadPool = backgroundThreadPool;
            this.backupTextService = backupTextService;
            this.name = "do";
            this.help = "Launch text backup";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            backgroundThreadPool.execute(() -> {
                log.info("Text backup started by {}", FormatUtils.formatAuthor(event));
                event.deferReply(true).queue();
                backupTextService.readAllChannelsHistories((success, message) -> {
                    try {
                        if (success) {
                            event.getHook().editOriginalFormat("%1$s Text was saved to database!", getClient().getSuccess()).queue();
                        } else {
                            event.getHook().editOriginalFormat("%1$s Text was not saved to database! (%2$s)", getClient().getError(), message).queue();
                        }
                    } catch (ErrorResponseException e) {
                        log.warn("Backup took too long.");
                        /* When defer reply takes too long */
                        event.getChannel().sendMessageFormat("Backup has %1$s.", success ? "finished" : "failed").queue();
                    }
                });
            });
        }
    }

    private static final class Export extends AdminCommand {

        private Export() {
            this.name = "export";
            this.help = "Export text into convenient format";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            event.reply("\uD83D\uDC45 Text export has not been implemented (and will not be)").setEphemeral(true).queue();
        }
    }
}
