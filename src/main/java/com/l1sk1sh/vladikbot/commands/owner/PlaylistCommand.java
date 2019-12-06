package com.l1sk1sh.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.services.PlaylistLoader;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.utils.CommandUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * - Addition of separate shuffle command
 * @author John Grosh
 */
public class PlaylistCommand extends OwnerCommand {
    private final Bot bot;
    private final String spacesLiteral = "\\s+";

    public PlaylistCommand(Bot bot) {
        this.bot = bot;
        this.name = "playlist";
        this.arguments = "<create|list|update|delete|default|shuffle>";
        this.help = "playlist management";
        this.guildOnly = false;
        this.children = new OwnerCommand[]{
                new ReadCommand(),
                new UpdateCommand(),
                new DeleteCommand(),
                new CreateCommand(),
                new DefaultListCommand(bot),
                new ShuffleCommand()
        };
    }

    @Override
    public void execute(CommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString());
    }

    class CreateCommand extends OwnerCommand {
        CreateCommand() {
            this.name = "create";
            this.aliases = new String[]{"make"};
            this.help = "makes a new playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll(spacesLiteral, "_");
            if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
                try {
                    bot.getPlaylistLoader().createPlaylist(pname);
                    event.replySuccess(String.format("Successfully created playlist `%1$s`!", pname));
                } catch (IOException e) {
                    event.replyError(String.format("Unable to create the playlist! `[%1$s]`", e.getLocalizedMessage()));
                }
            } else {
                event.replyError(String.format("Playlist `%1$s` already exists!", pname));
            }
        }
    }

    class ReadCommand extends OwnerCommand {
        ReadCommand() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "lists all available playlists";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            try {
                List<String> list = bot.getPlaylistLoader().getPlaylistNames();
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
            } catch (IOException ioe) {
                event.replyError(String.format("Local folder couldn't be processed! `[%1$s]`", ioe.getLocalizedMessage()));
            }
        }
    }

    class UpdateCommand extends OwnerCommand {
        UpdateCommand() {
            this.name = "add";
            this.aliases = new String[]{"append", "update"};
            this.help = "appends songs to an existing playlist";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] parts = event.getArgs().split(spacesLiteral, 2);
            if (parts.length < 2) {
                event.replyError("Please include a playlist name and URLs to add!");
                return;
            }
            String pname = parts[0];
            PlaylistLoader.Playlist playlist = bot.getPlaylistLoader().getPlaylist(pname);

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

                try {
                    bot.getPlaylistLoader().writePlaylist(pname, listOfUrlsToWrite);
                    event.replySuccess(String.format(
                            "Successfully added %1$s items to playlist `%2$s`.", urls.length, pname));
                } catch (IOException e) {
                    event.replyError(String.format("Unable to append the playlist! `[%1$s]`", e.getLocalizedMessage()));
                }
            }
        }
    }

    class DeleteCommand extends OwnerCommand {
        DeleteCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll(spacesLiteral, "_");
            if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
                event.replyError(String.format("Playlist `%1$s` doesn't exist!", pname));
            } else {
                try {
                    bot.getPlaylistLoader().deletePlaylist(pname);
                    event.replySuccess(String.format("Successfully deleted playlist `%1$s`.", pname));
                } catch (IOException e) {
                    event.replyError(String.format("Unable to delete the playlist! `[%1$s]`", e.getLocalizedMessage()));
                }
            }
        }

    }

    static class DefaultListCommand extends AutoPlaylistCommand {
        DefaultListCommand(Bot bot) {
            super(bot);
            this.name = "default";
            this.aliases = new String[]{"setdefault"};
            this.arguments = "<name|none>";
            this.guildOnly = true;
        }
    }

    class ShuffleCommand extends OwnerCommand {
        ShuffleCommand() {
            this.name = "shuffle";
            this.aliases = new String[]{"mix"};
            this.help = "shuffles specified playlist";
            this.arguments = "<name>";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll(spacesLiteral, "_");
            if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
                event.replyError(String.format("Playlist `%1$s` doesn't exist!", pname));
            } else {
                try {
                    List<String> list = bot.getPlaylistLoader().getPlaylistNames();
                    if (list == null) {
                        event.replyError("Failed to load available playlists!");
                    } else {
                        bot.getPlaylistLoader().shuffle(pname);
                        event.replySuccess(String.format("Successfully shuffled playlist `%1$s`!", pname));
                    }
                } catch (IOException e) {
                    event.replyError(String.format("Unable to suffle the playlist! `[%1$s]`", e.getLocalizedMessage()));
                }
            }
        }
    }
}
