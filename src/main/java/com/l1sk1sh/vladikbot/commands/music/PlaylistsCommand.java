package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.services.audio.PlaylistLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public class PlaylistsCommand extends MusicCommand {

    private final PlaylistLoader playlistLoader;

    @Autowired
    public PlaylistsCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager, PlaylistLoader playlistLoader) {
        super(guildSettingsRepository, playerManager);
        this.playlistLoader = playlistLoader;
        this.name = "mplaylists";
        this.help = "Shows the available playlists";
        this.beListening = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        List<String> list = playlistLoader.getPlaylistNames();
        if (list == null) {
            event.replyFormat("%1$s Failed to load available playlists!", event.getClient().getError()).setEphemeral(true).queue();
        } else if (list.isEmpty()) {
            event.replyFormat("%1$s There are no playlists!", event.getClient().getError()).setEphemeral(true).queue();
        } else {
            String message = event.getClient().getSuccess() + " Available playlists:\r\n";
            StringBuilder builder = new StringBuilder(message);
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\r\nType `").append(event.getClient().getTextualPrefix())
                    .append("play playlist <name>` to play a playlist");
            event.reply(builder.toString()).queue();
        }
    }
}
