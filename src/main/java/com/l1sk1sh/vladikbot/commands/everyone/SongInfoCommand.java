package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.network.dto.SongInfo;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
@Service
public class SongInfoCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(SongInfoCommand.class);

    private final RestTemplate restTemplate;

    @Autowired
    public SongInfoCommand() {
        this.restTemplate = new RestTemplate();
        this.name = "itunes";
        this.aliases = new String[]{"sinfo"};
        this.help = "get info on a song";
        this.arguments = "<song name>";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyWarning("Please include a song name.");

            return;
        }

        final String searchQuery = event.getArgs();
        final Pattern searchPattern = Pattern.compile("^[А-Яа-яA-Za-z0-9]++$");
        if (!searchPattern.matcher(searchQuery).matches()) {
            event.replyWarning("Search query should contain only cyrillic or latin letters and numbers.");

            return;
        }

        HttpUrl httpUrl = HttpUrl.parse("https://itunes.apple.com/");
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(httpUrl).newBuilder();
        urlBuilder.addPathSegment("search");
        urlBuilder.addQueryParameter("media", "music");
        urlBuilder.addQueryParameter("lang", "en_us");
        urlBuilder.addQueryParameter("limit", "1");
        urlBuilder.addEncodedQueryParameter("term", searchQuery);
        String url = urlBuilder.build().toString();

        SongInfo songInfo;
        try {
            songInfo = restTemplate.getForObject(url, SongInfo.class);
        } catch (RestClientException e) {
            event.replyError(String.format("Error occurred: `%1$s`", e.getLocalizedMessage()));
            log.error("Failed to consume API.", e);

            return;
        }

        if (songInfo == null) {
            log.error("Response body is empty.");
            event.replyWarning(String.format("Song `%1$s` was not found.", searchQuery));

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

        event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();
    }
}
