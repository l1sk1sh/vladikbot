package com.multiheaded.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.audio.AudioHandler;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class SkipToCommand extends DJCommand {
    public SkipToCommand(VladikBot bot) {
        super(bot);
        this.name = "skipto";
        this.help = "skips to the specified song";
        this.arguments = "<position>";
        this.aliases = new String[]{"jumpto"};
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        int index;
        try {
            index = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            event.reply(event.getClient().getError() + " `" + event.getArgs() + "` is not a valid integer!");
            return;
        }
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (index < 1 || index > audioHandler.getQueue().size()) {
            event.reply(event.getClient().getError()
                    + " Position must be a valid integer between 1 and " + audioHandler.getQueue().size() + "!");
            return;
        }
        audioHandler.getQueue().skip(index - 1);
        event.reply(event.getClient().getSuccess()
                + " Skipped to **" + audioHandler.getQueue().get(0).getTrack().getInfo().title + "**");
        audioHandler.getPlayer().stopTrack();
    }
}
