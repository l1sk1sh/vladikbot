package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.SteamStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.time.Instant;
import java.util.Date;

/**
 * @author l1sk1sh
 */
@Service
public class SteamStatusCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(SteamStatusCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public SteamStatusCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "steamstatus";
        this.help = "get status of steam services";
    }

    @Override
    protected void execute(CommandEvent event) {
        SteamStatus steamStatus;
        try {
            steamStatus = restTemplate.getForObject("https://crowbar.steamstat.us/Barney", SteamStatus.class);
        } catch (RestClientException e) {
            event.replyError(String.format("Error occurred: `%1$s`", e.getLocalizedMessage()));
            log.error("Failed to consume API.", e);

            return;
        }

        if (steamStatus == null) {
            log.error("Response body is empty.");
            event.replyWarning("Steam status api doesn't work.");

            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("Steam status", null, "https://upload.wikimedia.org/wikipedia/commons/c/c1/Steam_Logo.png")
                .setColor(new Color(120, 120, 120))
                .addField("Website", "Status: " + steamStatus.getWebsiteStatus() + " (" + steamStatus.getWebsiteOnline() + ")", true)
                .addField("Community website", "Status: " + steamStatus.getCommunityStatus() + " (" + steamStatus.getCommunityOnline() + ")", true)
                .addField("Database", "Status: " + steamStatus.getDatabaseHealth(), true)
                .setFooter("Updated at " + Date.from(Instant.ofEpochSecond(steamStatus.getTime())) + " | Online " + steamStatus.getOnline() + "%", null);

        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}
