package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlaylistLoader;
import com.l1sk1sh.vladikbot.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class AutoPlaylistCommand extends OwnerCommand {
    private static final Logger log = LoggerFactory.getLogger(AutoPlaylistCommand.class);

    private final PlaylistLoader playlistLoader;
    private final GuildSettingsRepository guildSettingsRepository;

    @Autowired
    public AutoPlaylistCommand(PlaylistLoader playlistLoader, GuildSettingsRepository guildSettingsRepository) {
        this.playlistLoader = playlistLoader;
        this.guildSettingsRepository = guildSettingsRepository;
        this.name = "autoplaylist";
        this.help = "sets the default playlist for the server";
        this.arguments = "<name|none>";
        this.guildOnly = true;
    }

    @Override
    public final void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a playlist name or none");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(settings -> {
                settings.setDefaultPlaylist(null);
                guildSettingsRepository.save(settings);
                String message = String.format("Cleared the default playlist for **%1$s**", event.getGuild().getName());
                log.info("{}. Cleared by {}.", message, FormatUtils.formatAuthor(event));
                event.replySuccess(message);
            });
            return;
        }

        String playlistName = event.getArgs().replaceAll("\\s+", "_");
        if (playlistLoader.getPlaylist(playlistName) == null) {
            event.replyError(String.format("Could not find `%1$s`!", playlistName));
        } else {
            guildSettingsRepository.findById(event.getGuild().getIdLong()).ifPresent(settings -> {
                settings.setDefaultPlaylist(playlistName);
                guildSettingsRepository.save(settings);
                event.replySuccess(String.format("The default playlist for **%1$s** is now `%2$s`",
                        event.getGuild().getName(), playlistName));
            });
        }
    }
}
