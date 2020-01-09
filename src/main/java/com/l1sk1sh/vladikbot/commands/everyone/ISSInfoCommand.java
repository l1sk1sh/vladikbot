package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.ISSInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

/**
 * @author Oliver Johnson
 */
public class ISSInfoCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(DogFactCommand.class);

    public ISSInfoCommand() {
        this.name = "space";
        this.help = "get ISS current info";
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            Request request = new Request.Builder()
                    .url("https://api.wheretheiss.at/v1/satellites/25544")
                    .build();

            Response response = Bot.httpClient.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("'wheretheiss.at' provided empty body.");
                event.replyWarning("Couldn't get IIS info.");

                return;
            }

            ISSInfo issInfo = Bot.gson.fromJson(body.string(), ISSInfo.class);

            Request requestAstros = new Request.Builder()
                    .url("http://api.open-notify.org/astros.json")
                    .build();

            Response responseAstros = Bot.httpClient.newCall(requestAstros).execute();
            ResponseBody bodyAstros = responseAstros.body();

            if (bodyAstros == null) {
                log.warn("'open-notify.org' provided empty body.");
            } else {
                issInfo.setAstronauts(Bot.gson.fromJson(bodyAstros.string(), ISSInfo.Astronauts.class));
            }

            MessageBuilder builder = new MessageBuilder();
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("ISS info", null, "https://www.stickpng.com/assets/images/58429400a6515b1e0ad75acc.png")
                    .setColor(new Color(20, 120, 100))
                    .addField("Location of the ISS", "lat " + issInfo.getLatitude() + "\r\nlon " + issInfo.getLongitude() + "\r\nalt " + issInfo.getAltitude(), true)
                    .addField("Current number of days in space", issInfo.getNumberOfDaysOnOrbit(), true)
                    .addField("Humans in space", issInfo.getPeopleNames(), true)
                    .setImage("http://businessforum.com/nasa01.JPEG");

            event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();

        } catch (IOException e) {
            log.error("Failed to retrieve a ISS info.", e);
            event.replyError("Failed to retrieve ISS info.");
        }
    }
}
