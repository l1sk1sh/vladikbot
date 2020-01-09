package com.l1sk1sh.vladikbot.commands.everyone;


import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.SteamStatus;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

/**
 * @author Oliver Johnson
 */
public class SteamStatusCommand extends Command  {
    private static final Logger log = LoggerFactory.getLogger(SteamStatusCommand.class);

    public SteamStatusCommand() {
        this.name = "steamstatus";
        this.help = "get status of steam services";
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Request request = new Request.Builder()
                    .url("https://crowbar.steamstat.us/Barney")
                    .build();

            Response response = Bot.httpClient.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("Response body is empty.");
                event.replyWarning("Steam status api doesn't work.");

                return;
            }

            SteamStatus steamStatus = Bot.gson.fromJson(body.string(), SteamStatus.class);
            MessageBuilder builder = new MessageBuilder();

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("Steam status", null, "https://upload.wikimedia.org/wikipedia/commons/c/c1/Steam_Logo.png")
                    .setColor(new Color(120, 120, 120))
                    .addField("Website", "Status: " + steamStatus.getWebsiteStatus() + " (" + steamStatus.getWebsiteOnline() + ")", true)
                    .addField("Community website", "Status: " + steamStatus.getCommunityStatus() + " (" + steamStatus.getCommunityOnline() + ")", true)
                    .addField("Database", "Status: " + steamStatus.getDatabaseHealth(), true)
                    .setFooter("Updated at " + Date.from(Instant.ofEpochSecond(steamStatus.getTime())) + " | Online " + steamStatus.getOnline() + "%", null);

            event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();

        } catch (IOException e) {
            log.error("Failed to retrieve Steam status.", e);
            event.replyError("Failed to retrieve Steam status.");
        }
    }

}
