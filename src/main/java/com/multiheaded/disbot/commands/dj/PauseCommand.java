package com.multiheaded.disbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.Bot;
import com.multiheaded.disbot.audio.AudioHandler;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class PauseCommand extends DJCommand {
    public PauseCommand(Bot bot) {
        super(bot);
        this.name = "pause";
        this.help = "pauses the current song";
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getPlayer().isPaused()) {
            event.replyWarning("The player is already paused! Use `"
                    + event.getClient().getPrefix() + "play` to unpause!");
            return;
        }
        handler.getPlayer().setPaused(true);
        event.replySuccess("Paused **" + handler.getPlayer().getPlayingTrack().getInfo().title
                + "**. Type `" + event.getClient().getPrefix() + "play` to unpause!");
    }
}
