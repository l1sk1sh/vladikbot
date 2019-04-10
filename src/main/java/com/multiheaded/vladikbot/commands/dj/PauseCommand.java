package com.multiheaded.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.services.audio.AudioHandler;

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
            event.replyWarning(String.format("The player is already paused! Use `%1$splay` to unpause!",
                    event.getClient().getPrefix()));
            return;
        }
        audioHandler.getPlayer().setPaused(true);
        event.replySuccess(String.format("Paused **%1$s**. Type `%2$splay` to unpause!",
                audioHandler.getPlayer().getPlayingTrack().getInfo().title,
                event.getClient().getPrefix()));
    }
}
