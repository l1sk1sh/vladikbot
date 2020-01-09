package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.CountryInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
public class CountryCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(CatPictureCommand.class);

    public CountryCommand() {
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

        try {
            Request request = new Request.Builder()
                    .url("https://restcountries.eu/rest/v2/name/" + countryCode.toLowerCase())
                    .build();

            Response response = Bot.httpClient.newCall(request).execute();
            ResponseBody body = response.body();

            if (response.code() != 200) {
                event.replyWarning(String.format("Country `%1$s` was not found.", countryCode));

                return;
            }

            if (body == null) {
                log.error("Response body is empty.");
                event.replyWarning(String.format("Country `%1$s` was not found.", countryCode));

                return;
            }

            CountryInfo countryInfo = Bot.gson.fromJson(body.string(), CountryInfo[].class)[0];
            MessageBuilder builder = new MessageBuilder();

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(countryInfo.getName(), countryInfo.getFlag(), null)
                    .setColor(new Color(20, 120, 120))
                    .addField("Population", countryInfo.getFormattedPopulation(), true)
                    .addField("Capital City", countryInfo.getCapitalCity(), true)
                    .addField("Main currency", countryInfo.getMainCurrencyName()
                            + " (" + countryInfo.getMainCurrencySymbol() + ")", true)
                    .addField("Located in", countryInfo.getSubregion(), true)
                    .addField("Demonym", countryInfo.getDemonym(), true)
                    .addField("Native Name", countryInfo.getNativeName(), true)
                    .addField("Area", countryInfo.getFormattedArea() + " km", true);

            event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();

        } catch(IOException e) {
            log.error("Failed to retrieve country's info by query '{}'.", countryCode, e);
            event.replyError("Failed to retrieve country's info.");
        }
    }
}
