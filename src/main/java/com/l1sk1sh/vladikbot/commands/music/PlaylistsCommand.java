package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import com.l1sk1sh.vladikbot.services.audio.PlaylistLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class PlaylistsCommand extends MusicCommand {

    private final PlaylistLoader playlistLoader;

    @Autowired
    public PlaylistsCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager, PlaylistLoader playlistLoader) {
        super(guildSettingsRepository, playerManager);
        this.playlistLoader = playlistLoader;
        this.name = "playlists";
        this.aliases = new String[]{"pls"};
        this.help = "shows the available playlists";
        this.guildOnly = true;
        this.beListening = false;
        this.beListening = false;
    }

    @Override
    public void doCommand(CommandEvent event) {
        List<String> list = playlistLoader.getPlaylistNames();
        if (list == null) {
            event.replyError("Failed to load available playlists!");
        } else if (list.isEmpty()) {
            event.replyWarning("There are no playlists in the Playlists folder!");
        } else {
            String message = event.getClient().getSuccess() + " Available playlists:\r\n";
            StringBuilder builder = new StringBuilder(message);
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\r\nType `").append(event.getClient().getTextualPrefix())
                    .append("play playlist <name>` to play a playlist");
            event.reply(builder.toString());
        }
    }
}
