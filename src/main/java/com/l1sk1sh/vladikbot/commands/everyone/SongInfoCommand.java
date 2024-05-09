package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.network.dto.SongInfo;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class SongInfoCommand extends SlashCommand {

    private static final String SONG_NAME_OPTION_KEY = "song";

    private final RestTemplate restTemplate;

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
            event.replyFormat("%1$s Please include a song name.", event.getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        final String searchQuery = option.getAsString();
        final Pattern searchPattern = Pattern.compile("^[А-Яа-яA-Za-z0-9 ]++$");
        if (!searchPattern.matcher(searchQuery).matches()) {
            event.replyFormat("%1$s Search query should contain only cyrillic or latin letters and numbers.", event.getClient().getWarning()).setEphemeral(true).queue();

            return;
        }

        URI uri;
        try {
            URIBuilder builder = new URIBuilder("https://itunes.apple.com");
            uri = builder
                    .setPath("search")
                    .setParameter("media", "music")
                    .setParameter("lang", "en_us")
                    .setParameter("limit", "1")
                    .setParameter("term", searchQuery)
                    .build();
        } catch (URISyntaxException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to build URI for iTunes.", e);

            return;
        }

        SongInfo songInfo;
        try {
            songInfo = restTemplate.getForObject(uri, SongInfo.class);
        } catch (RestClientException e) {
            event.replyFormat("%1$s Error occurred: `%2$s`", event.getClient().getError(), e.getLocalizedMessage()).setEphemeral(true).queue();
            log.error("Failed to consume API.", e);

            return;
        }

        if (songInfo == null || songInfo.getResults().length == 0) {
            event.replyFormat("%1$s Song `%2$s` was not found.", event.getClient().getError(), searchQuery).setEphemeral(true).queue();
            log.error("Response body is empty.");

            return;
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();
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
