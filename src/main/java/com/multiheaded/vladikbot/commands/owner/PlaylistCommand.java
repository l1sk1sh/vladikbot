package com.multiheaded.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.models.playlist.PlaylistLoader.Playlist;

import java.io.IOException;
import java.util.List;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class PlaylistCommand extends OwnerCommand {
    private final VladikBot bot;

    public PlaylistCommand(VladikBot bot) {
        this.bot = bot;
        this.guildOnly = false;
        this.name = "playlist";
        this.arguments = "<append|delete|make|setdefault>";
        this.help = "playlist management";
        this.children = new OwnerCommand[]{
                new ListCommand(),
                new AppendListCommand(),
                new DeleteListCommand(),
                new MakeListCommand(),
                new DefaultListCommand(bot)
        };
    }

    @Override
    public void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " Playlist Management Commands:\n");
        for (Command cmd : this.children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments()
                    == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
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
                    event.reply(event.getClient().getSuccess()
                            + " Successfully created playlist `" + pname + "`!");
                } catch (IOException e) {
                    event.reply(event.getClient().getError()
                            + " I was unable to create the playlist: " + e.getLocalizedMessage());
                }
            } else
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` already exists!");
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
            if (bot.getPlaylistLoader().getPlaylist(pname) == null)
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!");
            else {
                try {
                    bot.getPlaylistLoader().deletePlaylist(pname);
                    event.reply(event.getClient().getSuccess()
                            + " Successfully deleted playlist `" + pname + "`!");
                } catch (IOException e) {
                    event.reply(event.getClient().getError()
                            + " I was unable to delete the playlist: " + e.getLocalizedMessage());
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
                event.reply(event.getClient().getError() + " Please include a playlist name and URLs to add!");
                return;
            }
            String pname = parts[0];
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(pname);
            if (playlist == null)
                event.reply(event.getClient().getError() + " Playlist `" + pname + "` doesn't exist!");
            else {
                StringBuilder builder = new StringBuilder();
                playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
                String[] urls = parts[1].split("\\|");
                for (String url : urls) {
                    String u = url.trim();
                    if (u.startsWith("<") && u.endsWith(">"))
                        u = u.substring(1, u.length() - 1);
                    builder.append("\r\n").append(u);
                }
                try {
                    bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
                    event.reply(event.getClient().getSuccess()
                            + " Successfully added " + urls.length + " items to playlist `" + pname + "`!");
                } catch (IOException e) {
                    event.reply(event.getClient().getError()
                            + " I was unable to append to the playlist: " + e.getLocalizedMessage());
                }
            }
        }
    }

    class DefaultListCommand extends AutoPlaylistCommand {
        DefaultListCommand(VladikBot bot) {
            super(bot);
            this.name = "setdefault";
            this.aliases = new String[]{"default"};
            this.arguments = "<playlistname|NONE>";
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
            if (!bot.getPlaylistLoader().folderExists())
                bot.getPlaylistLoader().createFolder();
            if (!bot.getPlaylistLoader().folderExists()) {
                event.reply(event.getClient().getWarning()
                        + " Playlists folder does not exist and could not be created!");
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if (list == null)
                event.reply(event.getClient().getError() + " Failed to load available playlists!");
            else if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " There are no playlists in the Playlists folder!");
            else {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " Available playlists:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }
}
