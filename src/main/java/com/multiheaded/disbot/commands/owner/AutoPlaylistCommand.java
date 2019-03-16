package com.multiheaded.disbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.VladikBot;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;

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
        this.guildOnly = true;
        this.name = "autoplaylist";
        this.arguments = "<name|NONE>";
        this.help = "sets the default playlist for the server";
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " Please include a playlist name or NONE");
            return;
        }
        if (event.getArgs().equalsIgnoreCase("none")) {
            Settings settings = SettingsManager.getInstance().getSettings();
            settings.setDefaultPlaylist(null);
            event.reply(event.getClient().getSuccess()
                    + " Cleared the default playlist for **" + event.getGuild().getName() + "**");
            return;
        }

        String pname = event.getArgs().replaceAll("\\s+", "_");
        if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.reply(event.getClient().getError() + " Could not find `" + pname + ".txt`!");
        } else {
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(pname);
            event.reply(event.getClient().getSuccess()
                    + " The default playlist for **" + event.getGuild().getName() + "** is now `" + pname + "`");
        }
    }
}
