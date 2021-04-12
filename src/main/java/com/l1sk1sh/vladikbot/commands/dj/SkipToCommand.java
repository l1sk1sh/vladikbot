package com.l1sk1sh.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;

import java.util.Objects;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SkipToCommand extends DJCommand {
    public SkipToCommand(Bot bot) {
        super(bot);
        this.name = "skipto";
        this.aliases = new String[]{"jumpto"};
        this.help = "skips to the specified song";
        this.arguments = "<position>";
        this.bePlaying = true;
    }

    @Override
    public final void doCommand(CommandEvent event) {
        int index;
        try {
            index = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            event.replyError(String.format("`%1$s` is not a valid integer!", event.getArgs()));
            return;
        }
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if ((index < 1) || (index > Objects.requireNonNull(audioHandler).getQueue().size())) {
            event.replyError(String.format("Position must be a valid integer between 1 and %1$s!", Objects.requireNonNull(audioHandler).getQueue().size()));
            return;
        }
        audioHandler.getQueue().skip(index - 1);
        event.replySuccess(String.format("Skipped to **%1$s**.", audioHandler.getQueue().get(0).getTrack().getInfo().title));
        audioHandler.getPlayer().stopTrack();
    }
}
