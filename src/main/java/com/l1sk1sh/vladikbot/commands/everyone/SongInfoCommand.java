package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.l1sk1sh.vladikbot.network.dto.SongInfo;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.UriBuilder;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author l1sk1sh
 */
@Service
public class SongInfoCommand extends SlashCommand {
    private static final Logger log = LoggerFactory.getLogger(SongInfoCommand.class);

    private static final String SONG_NAME_OPTION_KEY = "song";

    private final RestTemplate restTemplate;

    @Autowired
    public SongInfoCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "songinfo";
        this.help = "Get info on a song";
        this.options = Collections.singletonList(new OptionData(OptionType.STRING, SONG_NAME_OPTION_KEY, "Song to search for").setRequired(true));
        registerMessageConverter();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        OptionMapping option = event.getOption(SONG_NAME_OPTION_KEY);
        if (option == null) {
            event.reply("Please include a song name.").setEphemeral(true).queue();

            return;
        }

        final String searchQuery = option.getAsString();
        final Pattern searchPattern = Pattern.compile("^[А-Яа-яA-Za-z0-9 ]++$");
        if (!searchPattern.matcher(searchQuery).matches()) {
            event.reply("Search query should contain only cyrillic or latin letters and numbers.").setEphemeral(true).queue();

            return;
        }

        URI uri = UriBuilder.fromUri("https://itunes.apple.com")
                .path("search")
                .queryParam("media", "music")
                .queryParam("lang", "en_us")
                .queryParam("limit", "1")
                .queryParam("term", searchQuery)
                .build(false);

        SongInfo songInfo;
        try {
            songInfo = restTemplate.getForObject(uri, SongInfo.class);
        } catch (RestClientException e) {
            event.replyFormat("Error occurred: `%1$s`", e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (songInfo == null || songInfo.getResults().length == 0) {
            event.replyFormat("Song `%1$s` was not found.", searchQuery).setEphemeral(true).queue();
            log.error("Response body is empty.");

            return;
        }

        MessageBuilder builder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setAuthor(songInfo.getTrackName(), songInfo.getTrackViewUrl(), songInfo.getArtworkUrl100())
                .setColor(new Color(20, 120, 120))
                .setThumbnail(songInfo.getArtworkUrl100())
                .addField("Song info", songInfo.getTrackName(), true)
                .addField("Artist", songInfo.getArtistName(), true)
                .addField("Album", songInfo.getCollectionName(), true)
                .setFooter("Genre: " + songInfo.getPrimaryGenreName() + " | Release date: "
                        + FormatUtils.getDateFromDatetime(songInfo.getReleaseDate()), null);

        event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
    }

    /**
     * iTunes returns media type [text/javascript;charset=utf-8] while actual media type is [application/json;charset=utf-8]
     * To fix this, restTemplate registers special media type converter
     */
    private void registerMessageConverter() {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);
    }
}
