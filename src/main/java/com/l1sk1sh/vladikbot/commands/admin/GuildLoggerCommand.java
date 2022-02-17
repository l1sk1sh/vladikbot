package com.l1sk1sh.vladikbot.commands.admin;

import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class GuildLoggerCommand extends AdminCommand {

    private static final String LOG_OPTION_KEY = "log";

    private final BotSettingsManager settings;

    @Autowired
    private GuildLoggerCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "logger";
        this.help = "Configures admin guild logging";
        this.options = Collections.singletonList(new OptionData(OptionType.BOOLEAN, LOG_OPTION_KEY, "State of guild logging").setRequired(false));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        boolean currentSetting = settings.get().isLogGuildChanges();

        OptionMapping logOption = event.getOption(LOG_OPTION_KEY);
        if (logOption == null) {
            event.replyFormat("Guild logging is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();
            return;
        }

        boolean newSetting = logOption.getAsBoolean();

        if (currentSetting == newSetting) {
            event.replyFormat("Guild logging is `%1$s`", (currentSetting) ? "ON" : "OFF").setEphemeral(true).queue();

            return;
        }

        settings.get().setLogGuildChanges(newSetting);
        log.info("Guild logging changed to {} by {}", newSetting, FormatUtils.formatAuthor(event));
        event.replyFormat("Guild logging is `%1$s`", (newSetting) ? "ON" : "OFF").setEphemeral(true).queue();
    }
}
