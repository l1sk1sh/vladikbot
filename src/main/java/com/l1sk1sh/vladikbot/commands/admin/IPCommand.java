package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.settings.BotSettingsManager;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

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
            ip = "not set";
        }

        String externalIp = null;

        try {
            URL checkIP = new URL("https://checkip.amazonaws.com");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    checkIP.openStream()))) {
                externalIp = in.readLine();
            }
        } catch (MalformedURLException ignored) {
        } catch (IOException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
        }

        String externalIpMessage = (externalIp != null) ? "\nExternal IP address (might be hidden by proxy or VPN) is: " + externalIp : "";
        event.replyFormat("Public IP (set manually by owner) is: `%1$s`%2$s", ip, externalIpMessage).setEphemeral(true).queue();
    }
}
