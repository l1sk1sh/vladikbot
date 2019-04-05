package com.multiheaded.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.services.PlaylistLoader.Playlist;

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
    private final VladikBot bot;

    public PlaylistCommand(VladikBot bot) {
        this.bot = bot;
        this.guildOnly = false;
        this.name = "playlist";
        this.arguments = "<append|delete|make|setdefault|shuffle>";
        this.help = "playlist management";
        this.children = new OwnerCommand[]{
                new ListCommand(),
                new AppendListCommand(),
                new DeleteListCommand(),
                new MakeListCommand(),
                new DefaultListCommand(bot),
                new ShuffleCommand()
        };
    }

    @Override
    public void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Playlist Management Commands:\n");
        for (Command cmd : this.children) {
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments()
                    == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        }
        event.reply(builder.toString());
    }

    class MakeListCommand extends OwnerCommand {
        MakeListCommand() {
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "makes a new playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
                try {
                    bot.getPlaylistLoader().createPlaylist(pname);
                    event.replySuccess("Successfully created playlist `" + pname + "`!");
                } catch (IOException e) {
                    event.replyError("Unable to create the playlist: " + e.getLocalizedMessage());
                }
            } else
                event.replyError("Playlist `" + pname + "` already exists!");
        }
    }

    class DeleteListCommand extends OwnerCommand {
        DeleteListCommand() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "deletes an existing playlist";
            this.arguments = "<name>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
                event.replyError("Playlist `" + pname + "` doesn't exist!");
            } else {
                try {
                    bot.getPlaylistLoader().deletePlaylist(pname);
                    event.replySuccess("Successfully deleted playlist `" + pname + "`!");
                } catch (IOException e) {
                    event.replyError("Unable to delete the playlist: " + e.getLocalizedMessage());
                }
            }
        }
    }

    class AppendListCommand extends OwnerCommand {
        AppendListCommand() {
            this.name = "append";
            this.aliases = new String[]{"add"};
            this.help = "appends songs to an existing playlist";
            this.arguments = "<name> <URL> | <URL> | ...";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] parts = event.getArgs().split("\\s+", 2);
            if (parts.length < 2) {
                event.replyError("Please include a playlist name and URLs to add!");
                return;
            }
            String pname = parts[0];
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(pname);

            if (playlist == null) {
                event.replyError("Playlist `" + pname + "` doesn't exist!");
            } else {
                List<String> listOfUrlsToWrite = (
                        playlist.getItems() == null)
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
                    event.replySuccess("Successfully added " + urls.length + " items to playlist `" + pname + "`!");
                } catch (IOException e) {
                    event.replyError("Unable to append to the playlist: " + e.getLocalizedMessage());
                }
            }
        }
    }

    class DefaultListCommand extends AutoPlaylistCommand {
        DefaultListCommand(VladikBot bot) {
            super(bot);
            this.name = "setdefault";
            this.aliases = new String[]{"default"};
            this.arguments = "<name|NONE>";
            this.guildOnly = true;
        }
    }

    class ListCommand extends OwnerCommand {
        ListCommand() {
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
                    StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
                    list.forEach(str -> builder.append("`").append(str).append("` "));
                    event.reply(builder.toString());
                }
            } catch (IOException ioe) {
                event.replyError(String.format("Local folder couldn't be processed! `[%s]`", ioe.getLocalizedMessage()));
            }
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
            String pname = event.getArgs().replaceAll("\\s+", "_");
            if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
                event.replyError("Playlist `" + pname + "` doesn't exist!");
            } else {
                try {
                    List<String> list = bot.getPlaylistLoader().getPlaylistNames();
                    if (list == null) {
                        event.replyError("Failed to load available playlists!");
                    } else {
                        bot.getPlaylistLoader().shuffle(pname);
                        event.replySuccess("Successfully shuffled playlist `" + pname + "`!");
                    }
                } catch (IOException e) {
                    event.replyError("Unable to shuffle the playlist: " + e.getLocalizedMessage());
                }
            }
        }
    }
}
