package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Oliver Johnson
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * @author John Grosh
 */
@Service
public class NowPlayingCommand extends MusicCommand {

    private final NowPlayingHandler nowPlayingHandler;

    @Autowired
    public NowPlayingCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager, NowPlayingHandler nowPlayingHandler) {
        super(guildSettingsRepository, playerManager);
        this.nowPlayingHandler = nowPlayingHandler;
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
            nowPlayingHandler.clearLastNPMessage(event.getGuild());
        } else {
            event.reply(message, nowPlayingHandler::setLastNPMessage);
        }
    }
}
