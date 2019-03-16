package com.multiheaded.disbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.VladikBot;
import com.multiheaded.disbot.audio.AudioHandler;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import com.multiheaded.disbot.utils.FormatUtil;

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
        Settings settings = SettingsManager.getInstance().getSettings();
        int volume = audioHandler.getPlayer().getVolume();
        if (event.getArgs().isEmpty()) {
            event.reply(FormatUtil.volumeIcon(volume) + " Current volume is `" + volume + "`");
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
                settings.setVolume(nVolume);
                event.reply(FormatUtil.volumeIcon(nVolume) + " Volume changed from `" + volume + "` to `" + nVolume + "`");
            }
        }
    }

}
