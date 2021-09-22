package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.network.dto.CountryInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author l1sk1sh
 */
@Service
public class CountryCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(CountryCommand.class);

    private static final String ISO_CODE_OPTION_KEY = "country";

    private final RestTemplate restTemplate;

    @Autowired
    public CountryCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "country";
        this.help = "Get info on a country";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, ISO_CODE_OPTION_KEY, "2 or 3 letter ISO country code").setRequired(true));
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping isoCodeOption = event.getOption(ISO_CODE_OPTION_KEY);
        if (isoCodeOption == null) {
            event.replyFormat("1$s Include country code. Example: `country usa`", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        String countryCode = isoCodeOption.getAsString();
        final Pattern countryCodePattern = Pattern.compile("^[A-Za-z]{2,3}$");
        if (!countryCodePattern.matcher(countryCode).matches()) {
            event.replyFormat("1$s Country code should be exactly 2 or 3 latin letters.", getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        ResponseEntity<CountryInfo[]> response;
        try {
            response = restTemplate.getForEntity("https://restcountries.eu/rest/v2/name/" + countryCode.toLowerCase(), CountryInfo[].class);
        } catch (RestClientException e) {
            event.replyFormat("1$s Error occurred: `%2$s`", getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            event.replyFormat("1$s Country `%2$s` was not found.", getClient().getError(), countryCode).setEphemeral(true).queue();

            return;
        }

        CountryInfo countryInfo = Objects.requireNonNull(response.getBody())[0];

        if (countryInfo == null) {
            event.replyFormat("1$s Country `%2$s` was not found.", getClient().getError(), countryCode).setEphemeral(true).queue();
            log.error("Response body is empty.");

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

        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }
}
