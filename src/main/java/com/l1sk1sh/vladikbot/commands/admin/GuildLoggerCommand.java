package com.l1sk1sh.vladikbot.commands.admin;

import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class GuildLoggerCommand extends AdminCommand {

    private static final String LOG_OPTION_KEY = "log";

    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    private GuildLoggerCommand(GuildSettingsRepository guildSettingsRepository) {
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "logger";
        this.help = "Configures admin guild logging";
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, LOG_OPTION_KEY, "State of guild logging").setRequired(false));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Optional<GuildSettings> settings = guildSettingsRepository.findById(Objects.requireNonNull(event.getGuild()).getIdLong());
        boolean currentSetting = settings.map(GuildSettings::isLogGuildChanges).orElse(false);
        TextChannel notificationChannel = settings.map(guildSettings -> guildSettings.getNotificationChannel(event.getGuild())).orElse(null);

        OptionMapping logOption = event.getOption(LOG_OPTION_KEY);
        if (logOption == null) {
            event.replyFormat("Guild logging is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();
            return;
        }

        boolean newSetting = logOption.getAsBoolean();

        if (notificationChannel == null && newSetting) {
            event.replyFormat("%1$s Set notification channel first.", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        if (currentSetting == newSetting) {
            event.replyFormat("Guild logging is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();

            return;
        }

        settings.get().setLogGuildChanges(newSetting);
        guildSettingsRepository.save(settings.get());
        log.info("Guild logging changed to {} by {}", newSetting, FormatUtils.formatAuthor(event));
        event.replyFormat("Guild logging is `%1$s`", (newSetting) ? "ON" : "OFF").setEphemeral(true).queue();
    }
}
