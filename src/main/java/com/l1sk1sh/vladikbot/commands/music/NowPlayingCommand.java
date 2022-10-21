package com.l1sk1sh.vladikbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.services.audio.AudioHandler;
import com.l1sk1sh.vladikbot.services.audio.NowPlayingHandler;
import com.l1sk1sh.vladikbot.services.audio.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author l1sk1sh
 * Changes from original source:
 * - Reformatted code
 * - DI Spring
 * - Moving to JDA-Chewtils
 * @author John Grosh
 */
@Service
public class NowPlayingCommand extends MusicCommand {

    private final NowPlayingHandler nowPlayingHandler;

    @Autowired
    public NowPlayingCommand(GuildSettingsRepository guildSettingsRepository, PlayerManager playerManager, NowPlayingHandler nowPlayingHandler) {
        super(guildSettingsRepository, playerManager);
        this.nowPlayingHandler = nowPlayingHandler;
        this.name = "mcurrent";
        this.help = "Shows the song that is currently playing";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        AudioHandler audioHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        MessageCreateData message = Objects.requireNonNull(audioHandler).getNowPlaying(event.getJDA());
        if (message == null) {
            event.reply(audioHandler.getNoMusicPlaying(event.getJDA())).queue();
            nowPlayingHandler.clearLastNPMessage(event.getGuild());
        } else {
            event.reply(message).queue();
            nowPlayingHandler.setLastNPMessage(event.getHook().retrieveOriginal().complete());
        }
    }
}
