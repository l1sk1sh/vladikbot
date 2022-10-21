package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.ISSInfo;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class ISSInfoCommand extends SlashCommand {

    private final RestTemplate restTemplate;

    @Autowired
    public ISSInfoCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "space";
        this.help = "Get ISS current info";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        ISSInfo issInfo;
        try {
            issInfo = restTemplate.getForObject("https://api.wheretheiss.at/v1/satellites/25544", ISSInfo.class);
        } catch (RestClientException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (issInfo == null) {
            event.replyFormat("%1$s Couldn't get IIS info.", event.getClient().getError()).setEphemeral(true).queue();
            log.error("'wheretheiss.at' provided empty body.");

            return;
        }

        ISSInfo.Astronauts astronauts;
        try {
            astronauts = restTemplate.getForObject("http://api.open-notify.org/astros.json", ISSInfo.Astronauts.class);
        } catch (RestClientException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (astronauts == null) {
            log.warn("'open-notify.org' provided empty body.");
        } else {
            issInfo.setAstronauts(astronauts);
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor("ISS info", null, "https://www.stickpng.com/assets/images/58429400a6515b1e0ad75acc.png")
                .setColor(new Color(20, 120, 100))
                .addField("Location of the ISS", "lat " + issInfo.getLatitude() + "\r\nlon " + issInfo.getLongitude() + "\r\nalt " + issInfo.getAltitude(), true)
                .addField("Current number of days in space", issInfo.getNumberOfDaysOnOrbit(), true)
                .addField("Humans in space", issInfo.getPeopleNames(), true)
                .setImage("http://businessforum.com/nasa01.JPEG");

        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
