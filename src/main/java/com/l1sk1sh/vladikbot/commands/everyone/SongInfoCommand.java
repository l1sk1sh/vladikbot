package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.domain.SongInfo;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Oliver Johnson
 */
public class SongInfoCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(SongInfoCommand.class);
    private final OkHttpClient client;

    public SongInfoCommand() {
        this.name = "itunes";
        this.aliases = new String[]{"sinfo"};
        this.help = "get info on a song";
        this.arguments = "<song name>";
        this.client = new OkHttpClient();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyWarning("Please include a song name.");

            return;
        }

        String searchQuery = event.getArgs();
        final Pattern searchPattern = Pattern.compile("^[А-Яа-яA-Za-z0-9]++$");
        if (!searchPattern.matcher(searchQuery).matches()) {
            event.replyWarning("Search query should contain only cyrillic or latin letters and numbers.");

            return;
        }

        try {
            HttpUrl httpUrl = HttpUrl.parse("https://itunes.apple.com/");
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(httpUrl).newBuilder();
            urlBuilder.addPathSegment("search");
            urlBuilder.addQueryParameter("media", "music");
            urlBuilder.addQueryParameter("lang", "en_us");
            urlBuilder.addQueryParameter("limit", "1");
            urlBuilder.addEncodedQueryParameter("term", searchQuery);
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            if (body == null) {
                log.error("Response body is empty.");
                event.replyWarning(String.format("Song `%1$s` was not found.", searchQuery));

                return;
            }

            SongInfo songInfo = Bot.gson.fromJson(body.string(), SongInfo.class);
            MessageBuilder builder = new MessageBuilder();

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor(songInfo.getTrackName(), songInfo.getTrackViewUrl(), songInfo.getArtworkUrl100())
                    .setColor(new Color(20, 120, 120))
                    .setThumbnail(songInfo.getArtworkUrl100())
                    .addField("Song info", songInfo.getTrackName(), true)
                    .addField("Artist", songInfo.getArtistName(), true)
                    .addField("Album", songInfo.getCollectionName(), true)
                    .setFooter("Genre: " + songInfo.getPrimaryGenreName() + " | Release date: "
                            + FormatUtils.getDate(songInfo.getReleaseDate()), null);

            event.getChannel().sendMessage(builder.setEmbed(embedBuilder.build()).build()).queue();

        } catch (IOException e) {
            log.error("Failed to retrieve song's info by query '{}'.", searchQuery, e);
            event.replyError("Failed to retrieve song's info.");
        }
    }
}
