package com.multiheaded.vladikbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.settings.Settings;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class AutoPlaylistCommand extends OwnerCommand {
    private final VladikBot bot;

    public AutoPlaylistCommand(VladikBot bot) {
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
            bot.getSettings().setDefaultPlaylist(null);
            event.replySuccess(String.format("Cleared the default playlist for **%1$s**", event.getGuild().getName()));
            return;
        }

        String pname = event.getArgs().replaceAll("\\s+", "_");
        if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.replyError(String.format("Could not find `%1$s`!", pname));
        } else {
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(pname);
            event.replySuccess(String.format("The default playlist for **%1$s** is now `%2$s`",
                    event.getGuild().getName(), pname));
        }
    }
}
