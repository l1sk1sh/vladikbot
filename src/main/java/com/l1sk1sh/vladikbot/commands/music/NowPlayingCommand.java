package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.Bot;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.util.Objects;

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
        this.aliases = new String[]{"np", "current"};
        this.help = "shows the song that is currently playing";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        Message message = Objects.requireNonNull(audioHandler).getNowPlaying(event.getJDA());
        if (message == null) {
            event.reply(audioHandler.getNoMusicPlaying(event.getJDA()));
            bot.getNowPlayingHandler().clearLastNPMessage(event.getGuild());
        } else {
            event.reply(message, msg -> bot.getNowPlayingHandler().setLastNPMessage(msg));
        }
    }
}
