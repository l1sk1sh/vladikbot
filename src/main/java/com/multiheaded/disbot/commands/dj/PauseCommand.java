package com.multiheaded.disbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.VladikBot;
import com.multiheaded.disbot.audio.AudioHandler;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class PauseCommand extends DJCommand {
    public PauseCommand(VladikBot bot) {
        super(bot);
        this.name = "pause";
        this.help = "pauses the current song";
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (audioHandler.getPlayer().isPaused()) {
            event.replyWarning("The player is already paused! Use `"
                    + event.getClient().getPrefix() + "play` to unpause!");
            return;
        }
        audioHandler.getPlayer().setPaused(true);
        event.replySuccess("Paused **" + audioHandler.getPlayer().getPlayingTrack().getInfo().title
                + "**. Type `" + event.getClient().getPrefix() + "play` to unpause!");
    }
}
