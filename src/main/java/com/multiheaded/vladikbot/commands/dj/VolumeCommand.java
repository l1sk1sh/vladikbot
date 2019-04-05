package com.multiheaded.vladikbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.vladikbot.VladikBot;
import com.multiheaded.vladikbot.services.audio.AudioHandler;
import com.multiheaded.vladikbot.utils.FormatUtils;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class VolumeCommand extends DJCommand {
    public VolumeCommand(VladikBot bot) {
        super(bot);
        this.name = "volume";
        this.aliases = new String[]{"vol"};
        this.help = "sets or shows volume";
        this.arguments = "[0-150]";
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int volume = audioHandler.getPlayer().getVolume();
        if (event.getArgs().isEmpty()) {
            event.reply(FormatUtils.volumeIcon(volume) + " Current volume is `" + volume + "`");
        } else {
            int nVolume;
            try {
                nVolume = Integer.parseInt(event.getArgs());
            } catch (NumberFormatException e) {
                nVolume = -1;
            }
            if (nVolume < 0 || nVolume > 150) {
                event.reply(event.getClient().getError() + " Volume must be a valid integer between 0 and 150!");
            } else {
                audioHandler.getPlayer().setVolume(nVolume);
                bot.getSettings().setVolume(nVolume);
                event.reply(FormatUtils.volumeIcon(nVolume) + " Volume changed from `" + volume + "` to `" + nVolume + "`");
            }
        }
    }

}
