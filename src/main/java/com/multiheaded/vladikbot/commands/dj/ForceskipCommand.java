package com.multiheaded.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.services.audio.AudioHandler;
import net.dv8tion.jda.core.entities.User;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class ForceskipCommand extends DJCommand {
    public ForceskipCommand(VladikBot bot) {
        super(bot);
        this.name = "forceskip";
        this.help = "skips the current song";
        this.aliases = new String[]{"modskip"};
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        User user = event.getJDA().getUserById(audioHandler.getRequester());
        event.reply(event.getClient().getSuccess()
                + " Skipped **" + audioHandler.getPlayer().getPlayingTrack().getInfo().title
                + "** (requested by " + (user == null ? "someone" : "**" + user.getName() + "**") + ")");
        audioHandler.getPlayer().stopTrack();
    }
}
