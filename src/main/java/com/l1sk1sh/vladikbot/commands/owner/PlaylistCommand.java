package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.entity.Playlist;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.PlaylistLoader;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - Addition of separate shuffle command
 * - DI Spring
 * @author John Grosh
 */
@Service
public class PlaylistCommand extends OwnerCommand {
    private static final String SPACES_LITERAL = "\\s+";

    private final PlaylistLoader playlistLoader;

    @Autowired
    public PlaylistCommand(GuildSettingsRepository guildSettingsRepository, PlaylistLoader playlistLoader) {
        this.playlistLoader = playlistLoader;
        this.name = "playlist";
        this.arguments = "<create|list|update|delete|default|shuffle>";
        this.help = "playlist management";
        this.guildOnly = false;
        this.children = new OwnerCommand[]{
                new ReadCommand(),
                new UpdateCommand(),
                new DeleteCommand(),
                new CreateCommand(),
                new ShuffleCommand(),
                new DefaultListCommand(guildSettingsRepository)
        };
    }

    @Override
    public void execute(CommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
    }

    private class CreateCommand extends OwnerCommand {
        private CreateCommand() {
            this.name = "create";
            this.aliases = new String[]{"make"};
            this.help = "makes a new playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll(SPACES_LITERAL, "_");
            if (playlistLoader.getPlaylist(pname) == null) {
                playlistLoader.createPlaylist(pname);
                event.replySuccess(String.format("Successfully created playlist `%1$s`!", pname));
            } else {
                event.replyError(String.format("Playlist `%1$s` already exists!", pname));
            }
        }
    }

    private class ReadCommand extends OwnerCommand {
        private ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "lists all available playlists";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            List<String> list = playlistLoader.getPlaylistNames();
            if (list == null) {
                event.replyError("Failed to load available playlists!");
            } else if (list.isEmpty()) {
                event.replyWarning("There are no playlists in the Playlists folder!");
            } else {
                String message = event.getClient().getSuccess() + " Available playlists:\r\n";
                StringBuilder builder = new StringBuilder(message);
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }

    private class UpdateCommand extends OwnerCommand {
        private UpdateCommand() {
            this.name = "add";
            this.aliases = new String[]{"append", "update"};
            this.help = "appends songs to an existing playlist";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] parts = event.getArgs().split(SPACES_LITERAL, 2);
            if (parts.length < 2) {
                event.replyError("Please include a playlist name and URLs to add!");
                return;
            }
            String pname = parts[0];
            Playlist playlist = playlistLoader.getPlaylist(pname);

            if (playlist == null) {
                event.replyError(String.format("Playlist `%1$s` doesn't exist!", pname));
            } else {
                List<String> listOfUrlsToWrite = (playlist.getItems() == null)
                        ? new ArrayList<>()
                        : new ArrayList<>(playlist.getItems()
                );

                String[] urls = parts[1].split("\\|");
                for (String url : urls) {
                    String u = url.trim();
                    if (u.startsWith("<") && u.endsWith(">")) {
                        u = u.substring(1, u.length() - 1);
                    }
                    listOfUrlsToWrite.add(u);
                }

                playlistLoader.writePlaylist(pname, listOfUrlsToWrite);
                event.replySuccess(String.format(
                        "Successfully added %1$s items to playlist `%2$s`.", urls.length, pname));
            }
        }
    }

    private class DeleteCommand extends OwnerCommand {
        private DeleteCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll(SPACES_LITERAL, "_");
            if (playlistLoader.getPlaylist(pname) == null) {
                event.replyError(String.format("Playlist `%1$s` doesn't exist!", pname));
            } else {
                if (playlistLoader.deletePlaylist(pname)) {
                    event.replySuccess(String.format("Successfully deleted playlist `%1$s`.", pname));
                } else {
                    event.replyError("Unable to delete the playlist!");
                }
            }
        }

    }

    private class ShuffleCommand extends OwnerCommand {
        private ShuffleCommand() {
            this.name = "shuffle";
            this.aliases = new String[]{"mix"};
            this.help = "shuffles specified playlist";
            this.arguments = "<name>";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll(SPACES_LITERAL, "_");
            if (playlistLoader.getPlaylist(pname) == null) {
                event.replyError(String.format("Playlist `%1$s` doesn't exist!", pname));
            } else {
                List<String> list = playlistLoader.getPlaylistNames();
                if (list == null) {
                    event.replyError("Failed to load available playlists!");
                } else {
                    if (playlistLoader.shuffle(pname)) {
                        event.replySuccess(String.format("Successfully shuffled playlist `%1$s`!", pname));
                    } else {
                        event.replyError("Unable to suffle the playlist!");
                    }
                }
            }
        }
    }

    private class DefaultListCommand extends AutoPlaylistCommand {
        private DefaultListCommand(GuildSettingsRepository guildSettingsRepository) {
            super(playlistLoader, guildSettingsRepository);
            this.name = "default";
            this.aliases = new String[]{"setdefault"};
            this.arguments = "<name|none>";
            this.guildOnly = true;
        }
    }
}
