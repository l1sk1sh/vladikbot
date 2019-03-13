package com.multiheaded.disbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.multiheaded.disbot.Bot;
import com.multiheaded.disbot.audio.AudioHandler;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformating code
 * @author John Grosh
 */
public class NowPlayingCommand extends MusicCommand {
    public NowPlayingCommand(Bot bot) {
        super(bot);
        this.name = "nowplaying";
        this.help = "shows the song that is currently playing";
        this.aliases = new String[]{"np", "current"};
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        Message message = handler.getNowPlaying(event.getJDA());
        if (message == null) {
            event.reply(handler.getNoMusicPlaying(event.getJDA()));
            bot.getNowPlayingHandler().clearLastNPMessage(event.getGuild());
        } else {
            event.reply(message, msg -> bot.getNowPlayingHandler().setLastNPMessage(msg));
        }
    }
}
