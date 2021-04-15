package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.CountryInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
@Service
public class CountryCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(CatPictureCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public CountryCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "country";
        this.help = "get info on a country using 2,3-letters ISO code";
        this.arguments = "<2,3-letters code>";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyWarning("Please include country code. Example: `country usa`");

            return;
        }

        String countryCode = event.getArgs();
        final Pattern countryCodePattern = Pattern.compile("^[A-Za-z]{2,3}$");
        if (!countryCodePattern.matcher(countryCode).matches()) {
            event.replyWarning("Country code should be exactly 2,3 latin letters.");

            return;
        }

        ResponseEntity<CountryInfo> response = restTemplate.getForEntity("https://restcountries.eu/rest/v2/name/" + countryCode.toLowerCase(), CountryInfo.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            event.replyWarning(String.format("Country `%1$s` was not found.", countryCode));

            return;
        }

        CountryInfo countryInfo = response.getBody();

        if (countryInfo == null) {
            log.error("Response body is empty.");
            event.replyWarning(String.format("Country `%1$s` was not found.", countryCode));

            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(countryInfo.getName(), countryInfo.getFlag(), null)
                .setColor(new Color(20, 120, 120))
                .addField("Population", countryInfo.getFormattedPopulation(), true)
                .addField("Capital City", countryInfo.getCapital(), true)
                .addField("Main currency", countryInfo.getMainCurrencyName()
                        + " (" + countryInfo.getMainCurrencySymbol() + ")", true)
                .addField("Located in", countryInfo.getSubregion(), true)
                .addField("Demonym", countryInfo.getDemonym(), true)
                .addField("Native Name", countryInfo.getNativeName(), true)
                .addField("Area", countryInfo.getFormattedArea() + " km", true);

        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}
