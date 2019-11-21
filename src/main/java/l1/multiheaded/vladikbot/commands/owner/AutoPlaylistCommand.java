package l1.multiheaded.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import l1.multiheaded.vladikbot.Bot;
import l1.multiheaded.vladikbot.settings.GuildSettings;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class AutoPlaylistCommand extends OwnerCommand {
    private final Bot bot;

    public AutoPlaylistCommand(Bot bot) {
        this.bot = bot;
        this.name = "autoplaylist";
        this.help = "sets the default playlist for the server";
        this.arguments = "<name|none>";
        this.guildOnly = true;
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("Please include a playlist name or none");
            return;
        }

        if (event.getArgs().equalsIgnoreCase("none")) {
            bot.getGuildSettings(event.getGuild()).setDefaultPlaylist(null);
            event.replySuccess(String.format("Cleared the default playlist for **%1$s**", event.getGuild().getName()));
            return;
        }

        String playlistName = event.getArgs().replaceAll("\\s+", "_");
        if (bot.getPlaylistLoader().getPlaylist(playlistName) == null) {
            event.replyError(String.format("Could not find `%1$s`!", playlistName));
        } else {
            GuildSettings settings = event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(playlistName);
            event.replySuccess(String.format("The default playlist for **%1$s** is now `%2$s`",
                    event.getGuild().getName(), playlistName));
        }
    }
}
