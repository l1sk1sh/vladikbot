package com.multiheaded.disbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.Bot;
import com.multiheaded.disbot.audio.AudioHandler;
import net.dv8tion.jda.core.entities.User;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class ForceskipCommand extends DJCommand {
    public ForceskipCommand(Bot bot) {
        super(bot);
        this.name = "forceskip";
        this.help = "skips the current song";
        this.aliases = new String[]{"modskip"};
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        User u = event.getJDA().getUserById(handler.getRequester());
        event.reply(event.getClient().getSuccess()
                + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title
                + "** (requested by " + (u == null ? "someone" : "**" + u.getName() + "**") + ")");
        handler.getPlayer().stopTrack();
    }
}
