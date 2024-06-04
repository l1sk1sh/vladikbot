package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 */
@Service
public class IPCommand extends AdminCommand {

    private final BotSettingsManager settings;

    public IPCommand(BotSettingsManager settings) {
        this.settings = settings;
        this.name = "ip";
        this.help = "Shows bot's public IP (it is updated by the owner manually)";
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        String ip = settings.get().getBotPublicIP();

        if (ip == null || ip.isEmpty()) {
            event.replyFormat("%1$s Public IP was not set. Ask owner of the bot for details", event.getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        event.replyFormat("Public IP is `%1$s`", ip).setEphemeral(true).queue();
    }
}
