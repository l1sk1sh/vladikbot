package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.ISSInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

/**
 * @author l1sk1sh
 */
@Service
public class ISSInfoCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(DogFactCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public ISSInfoCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "space";
        this.help = "get ISS current info";
    }

    @Override
    protected void execute(CommandEvent event) {
        ISSInfo issInfo;
        try {
            issInfo = restTemplate.getForObject("https://api.wheretheiss.at/v1/satellites/25544", ISSInfo.class);
        } catch (RestClientException e) {
            event.replyError(String.format("Error occurred: `%1$s`", e.getLocalizedMessage()));
            log.error("Failed to consume API.", e);

            return;
        }

        if (issInfo == null) {
            log.error("'wheretheiss.at' provided empty body.");
            event.replyWarning("Couldn't get IIS info.");

            return;
        }

        ISSInfo.Astronauts astronauts;
        try {
            astronauts = restTemplate.getForObject("http://api.open-notify.org/astros.json", ISSInfo.Astronauts.class);
        } catch (RestClientException e) {
            event.replyError(String.format("Error occurred: `%1$s`", e.getLocalizedMessage()));
            log.error("Failed to consume API.", e);

            return;
        }

        if (astronauts == null) {
            log.warn("'open-notify.org' provided empty body.");
        } else {
            issInfo.setAstronauts(astronauts);
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
    }
}
