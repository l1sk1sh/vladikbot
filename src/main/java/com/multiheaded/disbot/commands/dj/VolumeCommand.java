package com.multiheaded.disbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.Bot;
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
    public VolumeCommand(Bot bot) {
        super(bot);
        this.name = "volume";
        this.aliases = new String[]{"vol"};
        this.help = "sets or shows volume";
        this.arguments = "[0-150]";
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        Settings settings = SettingsManager.getInstance().getSettings();
        int volume = handler.getPlayer().getVolume();
        if (event.getArgs().isEmpty()) {
            event.reply(FormatUtil.volumeIcon(volume) + " Current volume is `" + volume + "`");
        } else {
            int nvolume;
            try {
                nvolume = Integer.parseInt(event.getArgs());
            } catch (NumberFormatException e) {
                nvolume = -1;
            }
            if (nvolume < 0 || nvolume > 150)
                event.reply(event.getClient().getError() + " Volume must be a valid integer between 0 and 150!");
            else {
                handler.getPlayer().setVolume(nvolume);
                settings.setVolume(nvolume);
                event.reply(FormatUtil.volumeIcon(nvolume) + " Volume changed from `" + volume + "` to `" + nvolume + "`");
            }
        }
    }

}
