package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class SetIPCommand extends OwnerCommand {

    private static final String IP_OPTION_KEY = "ip";

    private final BotSettingsManager settings;

    private SetIPCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "setip";
        this.help = "Set bot's public IP for easier access to the server";
        this.guildOnly = false;
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, IP_OPTION_KEY, "Public IP used to access server").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping statusOption = event.getOption(IP_OPTION_KEY);
        if (statusOption == null) {
            event.replyFormat("%1$s IP is required for this command.", event.getClient().getWarning()).setEphemeral(true).queue();
            return;
        }

        String newSetting = statusOption.getAsString();

        settings.get().setBotPublicIP(newSetting);

        log.info("Public bot's IP is change to {} by {}.", newSetting, FormatUtils.formatAuthor(event));
        event.replyFormat("Public IP is `%1$s`", newSetting).setEphemeral(true).queue();
    }
}