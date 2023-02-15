package com.l1sk1sh.vladikbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public IPCommand() {
        this.name = "ip";
        this.help = "Shows bot's local IPs";
        this.guildOnly = true;
    }

    @Override
    protected final void execute(SlashCommandEvent event) {
        try {
            URL checkIP = new URL("http://checkip.amazonaws.com");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    checkIP.openStream()))) {
                event.reply(String.format("%1$s External IP address (might be hidden by proxy or VPN): `%2$s`", event.getClient().getSuccess(), in.readLine())).setEphemeral(true).queue();
            }
        } catch (MalformedURLException ignored) {
        } catch (IOException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
        }
    }
}
